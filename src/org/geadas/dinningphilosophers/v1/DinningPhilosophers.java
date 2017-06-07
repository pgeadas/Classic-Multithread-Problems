package org.geadas.dinningphilosophers.v1;

import static org.geadas.dinningphilosophers.v1.DinningPhilosophers.HOW_MANY_TIMES;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author geadas
 */
public class DinningPhilosophers {

    final static int TOTAL_PHILOSOPHERS = 25;
    final static int HOW_MANY_TIMES = 50; //how many rounds of eating will the philosophers do
    static int TOTAL_FORKS = TOTAL_PHILOSOPHERS;
    public static boolean dinnerOn = true;

    public static void main(String[] args) {
        ExecutorService executors = Executors.newFixedThreadPool(TOTAL_PHILOSOPHERS);
        int id = 0;
        Table table = new Table(TOTAL_PHILOSOPHERS);

        for (int i = 0; i < TOTAL_PHILOSOPHERS; i++) {
            executors.submit(new Philosopher(++id, false, table));
        }

        executors.shutdown();
    }

}

class Table {

    Fork[] forks;

    Table(int howMany) {
        forks = new Fork[howMany];
        for (int i = 0; i < howMany; i++) {
            forks[i] = new Fork(false, i);
        }
    }
}

class Philosopher implements Runnable {

    int id;
    volatile boolean hasEaten;
    Table table;
    int eatCounter;

    Philosopher(int id, boolean hasEaten, Table table) {
        this.id = id;
        this.hasEaten = hasEaten;
        this.table = table;
        this.eatCounter = 0;
    }

    @Override
    public void run() {
        int count = 0;
        while (count < HOW_MANY_TIMES) {
            try {
                tryToGetFork(table.forks, this);
                count++;
            } catch (InterruptedException ex) {
                Logger.getLogger(Philosopher.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("Philosopher: " + id + " has eaten " + eatCounter + " times!");
    }

    public void tryToGetFork(Fork[] forks, Philosopher phi) throws InterruptedException {
        boolean flag = true;

        System.out.println("Philosopher: " + phi.id + " already ate " + phi.eatCounter + " times!");

        Fork f1 = forks[(id - 1) % forks.length];

        synchronized (f1) {
            if (!f1.isInUse) {
                gotFork(f1);
                f1.inUse();
            } else {
                forkIsTaken(f1);
                flag = false;
            }
        }

        if (!flag) {
            think();
        }

        Fork f2 = forks[(id) % forks.length];
        synchronized (f2) {
            if (!f2.isInUse) {
                gotFork(f2);
                f2.inUse();
                eat(phi);
            } else {
                forkIsTaken(f2);
                flag = false;
            }

            f2.putDown();
        }

        synchronized (f1) {
            f1.putDown();
        }

        if (!flag) {
            think();
        }

    }

    public void think() throws InterruptedException {
        System.out.println("Philosopher: " + id + " is thinking.");
        Thread.sleep(500);
    }

    public void eat(Philosopher phi) throws InterruptedException {
        System.out.println("Philosopher: " + id + " is eating.");
        phi.hasEaten = true;
        phi.eatCounter += 1;
        Thread.sleep(1000);
    }

    public void gotFork(Fork f) {
        System.out.println("Philosopher: " + id + " got Fork: " + f.id);
    }

    public void forkIsTaken(Fork f) {
        System.out.println("Philosopher: " + id + " - Fork " + f.id + " was already taken!");
    }

}

class Fork {

    int id;
    volatile boolean isInUse;

    Fork(boolean isInUse, int id) {
        this.isInUse = isInUse;
        this.id = id;
    }

    public void inUse() {
        isInUse = true;
    }

    public void putDown() {
        isInUse = false;
    }

}
