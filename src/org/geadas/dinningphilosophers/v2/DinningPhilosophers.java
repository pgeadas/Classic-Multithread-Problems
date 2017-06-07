package org.geadas.dinningphilosophers.v2;

import static org.geadas.dinningphilosophers.v2.DinningPhilosophers.HOW_MANY_TIMES;
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

    public static void main(String[] args) throws InterruptedException {
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
    boolean hasEaten;
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
                if (!tryToGetFork(table.forks, this)) {
                    think();
                }
                count++;
            } catch (InterruptedException ex) {
                Logger.getLogger(Philosopher.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("Philosopher: " + id + " has eaten " + eatCounter + " times!");
    }

    public boolean tryToGetFork(Fork[] forks, Philosopher phi) throws InterruptedException {
        Fork f1, f2;

        if (phi.hasEaten) { //avoids same philosopher to eat 2 times in a row, which minimizes starvation of threads
            phi.hasEaten = false;
            System.out.println("Philosopher: " + phi.id + " already ate " + phi.eatCounter + " times!");
            return false;
        }

        synchronized (forks) {
            f1 = forks[(id - 1) % forks.length];
            if (!f1.isInUse) {
                gotFork(f1);
                f1.inUse();
            } else {
                forkIsTaken(f1);
                return false;
            }

            f2 = forks[(id) % forks.length];

            if (!f2.isInUse) {
                gotFork(f2);
                f2.inUse();
            } else {
                forkIsTaken(f2);
                f1.putDown();
                return false;
            }

        }

        eat(phi);
        f1.putDown();
        f2.putDown();

        return true;
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
