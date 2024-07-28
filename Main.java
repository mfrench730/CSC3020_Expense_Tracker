import java.util.Scanner;
import java.util.ArrayList;
import java.io.FileWriter;
import java.io.IOException;

class Entry {
    protected double amount;
    protected boolean expense;
    protected String date;
    protected String category;

    public Entry(double amount, boolean expense, String date, String category) {
        this.amount = amount;
        this.expense = expense;
        this.date = date;
        this.category = category;
    }

    protected void printEntry() {
        if (expense) {
            System.out.printf("-$%-8.2f | %s | %s \n", amount, date, category);
        } else {
            System.out.printf(" $%-8.2f | %s | %s \n", amount, date, category);
        }
    }

    public String getEntry() {
        String s;
        if (expense) {
            s = String.format("-$%-8.2f | %15s | %15s \n", amount, date, category);
        }
        else {
            s = String.format(" $%-10.2f | %15s | %15s \n", amount, date, category);
        }
        return s;
    }

    public void copyEntry(Entry otherEntry) {
        this.amount = otherEntry.amount;
        this.expense = otherEntry.expense;
        this.date = otherEntry.date;
        this.category = otherEntry.category;
    }

}

class Ledger {
    private int ledgerSize;
    private int length;
    private double balance;
    private double limit;
    public Entry[] list;

    public Ledger() {
        ledgerSize = 10;
        length = 0;
        balance = 0;
        limit = 0;
        list = new Entry[ledgerSize];
    }

    Ledger(int ledgerSize, double balance, double limit) {
        this.ledgerSize = ledgerSize;
        this.balance = balance;
        this.limit = limit;
        length = 0;
        list = new Entry[ledgerSize];
    }

    public void addEntry(double amount, boolean expense, String date, String category) {
        // can only add to end
        Entry entry = new Entry(amount, expense, date, category);
        list[length] = entry;
        length++;
        if (length == ledgerSize - 1) {
            // double ledgerSize if the ledger is full after add
            ledgerSize *= 2;
        }
        if (expense) {
            balance -= amount;
        } else {
            balance += amount;
        }
    }

    public void deleteEntry(int cursor) {
        for (int i = cursor; i < length; i++) {
            list[i] = list[i+1];
        }
        length--;
    }

    public double totalExpense() {
        // get total of ledger
        double total = 0;
        for (int i = 0; i < length; i++) {
            if (list[i].expense) {
                total += list[i].amount;
            } else {
                total-= list[i].amount;
            }
        }
        return total;
    }

    boolean checkLimit() {
        if (limit == -1.0) {
            return true;
        }
        // used for debugging
//        System.out.printf("Total expense: %.2f | Limit: %.2f", this.totalExpense(), this.getLimit());
        return this.totalExpense() < limit;
    }

    // get methods
    int getLength() { return length; }
    double getBalance() { return balance; }
    double getLimit() { return limit; }


    void printLedger() {
        for (int i = 0; i < length; i++) {
            list[i].printEntry();
        }
    }

    // used for csv export
    public ArrayList<String[]> toArray() {
        // use ArrayList to be able to append for each entry
        ArrayList<String[]> data = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            String[] row = {
                    list[i].amount + "",
                    list[i].expense + "",
                    list[i].date,
                    list[i].category
            };
            data.add(row);
        }
        return data;
    }

}

class CsvWriter {
    public static void export(Ledger ledger) {

        ArrayList<String[]> data = ledger.toArray();

        String[] header = {"Amount", "Expense", "Date", "Category"};

        String filename = "expenses.csv";

        try (FileWriter writer = new FileWriter(filename)) {
            // header
            writeLine(writer, header);

            // data
            for (String[] row : data) {
                writeLine(writer, row);
            }

            System.out.println("CSV created successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void writeLine(FileWriter writer, String[] values) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            sb.append(values[i]);
            if (i < values.length - 1) {
                sb.append(",");
            }
        }
        sb.append("\n");
        writer.write(sb.toString());
    }
}

public class Main {

    public static void printHelp() {
        System.out.println("Press D to display current entries");
        System.out.println("Press E to add expense");
        System.out.println("Press C to add credit");
        System.out.println("Press P to print to CSV");
        System.out.println("Press Q to quit");
    }

    static Ledger initLedger(Scanner scanner) {

        double balance = -1.0;

        while (balance == -1.0) {
            System.out.print("Enter starting balance for ledger: ");
            if (scanner.hasNextDouble()) {
                balance = scanner.nextDouble();
            }
            else {
                System.out.println("Please enter a valid balance.");
                scanner.next();
            }
        }

        // ask if user wants an expense limit
        System.out.print("Would you like to set a limit? (y or n) ");
        double limit = -1.0;
        char input = scanner.next().charAt(0);
        if (input == 'y' || input == 'Y') {
            while (limit == -1.0) {
                System.out.print("Enter limit: ");
                if (scanner.hasNextDouble()) {
                    limit = scanner.nextDouble();
                } else {
                    System.out.println("Enter valid limit.");
                    scanner.next();
                }
            }
        }

        return new Ledger(10, balance, limit);
    }

    static void printLedger(Ledger ledger) {
        // used to format ledger output
        System.out.println("Current Ledger");
        ledger.printLedger();
        System.out.printf("Balance: %.2f", ledger.getBalance());
        System.out.println();
        System.out.println();
    }

    static void addEntry(Ledger ledger, Scanner scanner, boolean expense) {
        // used to format adding entry

        double amount = -1.0;
        String date;
        String category;

        // get amount
        System.out.print("Enter amount: ");
        while (amount == -1.0)
        {
            if (scanner.hasNextDouble()) {
                amount = scanner.nextDouble();
            }
            else {
                System.out.println("Please input a valid amount.");
            }
        }

        // get date
        System.out.print("Enter date: ");
        date = scanner.next();

        // get category
        System.out.print("Enter category: ");
        category = scanner.next();

        // add to ledger
        ledger.addEntry(amount, expense, date, category);
    }

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("Java Expense Tracker\n");
        Ledger ledger = initLedger(scanner);

        char input = '0';

        do {
            printHelp();
            if (scanner.hasNext()) {
                // will take only first char if multiple are given
                input = scanner.next().charAt(0);
            } else {
                System.out.println("Invalid input.");
            }
            switch (input) {

                case 'd':
                case 'D':
                    printLedger(ledger);
                    break;

                case 'e':
                case 'E':
                    addEntry(ledger, scanner, true);
                    break;

                case 'c':
                case 'C':
                    addEntry(ledger, scanner, false);
                    break;

                case 'p':
                case 'P':
                    CsvWriter.export(ledger);
                    break;

                case 'q':
                case 'Q':
                    break;
            }

            if (!ledger.checkLimit()) {
                System.out.println("--------------------------");
                System.out.println("WARNING: You have surpassed your set expense limit.");
                System.out.println("--------------------------");
                System.out.printf("Total expenses: %.2f | Expense limit: %.2f", ledger.totalExpense(), ledger.getLimit());
                System.out.println();
            }
        }
        while (input != 'Q' && input != 'q');




    }
}