package source;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

public class Main
{

    Scanner sc = new Scanner(System.in);
    
    int size = 3;
    int day;
    int bin_row;
    int bin_col;
    int warehouse_row;
    int warehouse_col;
    int total_fruits;
    int rack_capacity;
    int bin_capacity;
    int bins_used;
    int source_row;
    int source_col;
    int[] no_of_tray_used;
    Tray[][] tray_table;
    Bin[][] bins;
    Warehouse warehouse;
    String begin_date;
    String end_date;

    double[] bin_min = new double[bin_row];
    
    Main() throws ParseException
    {
        System.out.print("Enter Rows in a warehouse: ");
        warehouse_row = sc.nextInt();
        System.out.print("Enter Columns in a warehouse: ");
        warehouse_col = sc.nextInt();
        System.out.print("Enter rack capacity: ");
        rack_capacity = sc.nextInt();
        warehouse = new Warehouse(warehouse_row, warehouse_col, rack_capacity);
        System.out.print("Enter Bin Height Capacity: ");
        bin_capacity = sc.nextInt();
        // warehouse_col = day;
        System.out.println("Enter the Range of Date of Stock in DD/MM/YYYY format");
        sc.nextLine();
        System.out.print("Start Date: ");
        begin_date = sc.nextLine();
        System.out.print("End Date: ");
        end_date = sc.nextLine();
        System.out.print("Enter total fruits arriving: ");
        total_fruits = sc.nextInt();
        // day = 3, size = 3
        day = getDifferenceDays(begin_date, end_date) + 1;
        tray_table = new Tray[day][size];
        bin_row = day;
        bin_col = 5;
        bins = new Bin[bin_row][bin_col];
        bins_used = 0;
        no_of_tray_used = new int[day];
        bin_min = new double[bin_row];
    }

    Main(String[] args) throws ParseException
    {
        warehouse_row = Integer.parseInt(args[0]);
        warehouse_col = Integer.parseInt(args[1]);
        rack_capacity = Integer.parseInt(args[2]);
        warehouse = new Warehouse(warehouse_row, warehouse_col, rack_capacity);
        bin_capacity = Integer.parseInt(args[3]);
        // warehouse_col = day;
        begin_date = args[4];
        end_date = args[5];
        total_fruits = Integer.parseInt(args[6]);
        // day = 3, size = 3
        day = getDifferenceDays(begin_date, end_date) + 1;
        tray_table = new Tray[day][size];
        bin_row = day;
        bin_col = 5;
        bins = new Bin[bin_row][bin_col];
        bins_used = 0;
        no_of_tray_used = new int[day];
        bin_min = new double[bin_row];
    }

    void extract() throws IOException, ParseException
    {

        String filename = "source//data.csv";
        BufferedReader br = new BufferedReader(new FileReader(filename));

        String line;
        String begin_date = br.readLine();
        String end_date = br.readLine();

        int dateDiff = this.getDifferenceDays(begin_date, end_date);

        for(int i = 0; i < this.day; i++)
        {
            for(int j = 0; j < this.size; j++)
            {
                int height = (j == 0)? 50: (j == 1)? 75: 100;
                this.tray_table[i][j] = new Tray(size + 1 - j, size + 1 - j, height);
            }
        }
        for(int i = 0; i < bin_row; i++)
        {
            for(int j = 0; j < bin_col; j++)
            {
                this.bins[i][j] = new Bin(this.bin_capacity, i);
            }
        }

        while((line = br.readLine()) != null)
        {
            // System.out.println(line);
            DataStorage ds = new DataStorage(line);

            // System.out.println(ds.day + ", " + ds.month + ", " + ds.year + ", " + ds.size);

            dateDiff = this.getDifferenceDays(this.begin_date, ds.day + "/" + ds.month + "/20" + ds.year);
            int row = dateDiff;
            // System.out.println("Datediff: " + dateDiff);
            int col = (ds.size == 50)? 0: ((ds.size == 75)? 1: 2);

            // System.out.println("Row: " + row + " col: " + col);
            if(this.tray_table[row][col].hasSpace())
            {
                this.tray_table[row][col].add();
            }
            else
            {
                
                this.addToBinTable(this.tray_table[row][col], row, col);

                this.no_of_tray_used[dateDiff]++;
                this.tray_table[row][col] = new Tray(this.tray_table[row][col].row, this.tray_table[row][col].column, this.tray_table[row][col].height);
                this.tray_table[row][col].add();
            }
        }
        br.close();
    }

    void addToBinTable(Tray t, int row, int col)
    {
        for(int i = 0; i < this.bins.length; i++)
        {
            for(int j = 0; j < this.bins[0].length; j++)
            {
                if(bins[i][j].space_left < 50)
                {
                    // System.out.println("Bin got full");
                    warehouse.add(bins[i][j]);
                    bins[i][j] = new Bin(bins[i][j].max_capacity, bins[i][j].day);
                    this.bins_used++;
                }
            }
        }

        int bin = 0;
        bin_min[row] = bin_capacity + 1;

        for(int i = 0; i < bin_col; i++)
        {
            if(this.bins[row][i].hasSpace(this.tray_table[row][col].height) && this.bins[row][i].space_left - this.tray_table[row][col].height < bin_min[row])
            {
                // System.out.println("Inside adding if" + i);
                bin = i;
                bin_min[row] = this.bins[row][i].space_left - t.height;
            }
        }


        if(bin_min[row] == bin_capacity + 1)
        {
            int max_loaded = 0;

            for(int i = 1; i < bin_col; i++)
            {
                if(this.bins[row][max_loaded].occupied < this.bins[row][i].occupied)
                {
                    max_loaded = i;
                }
            }

            warehouse.add(this.bins[row][max_loaded]);
            this.bins_used++;
            this.bins[row][max_loaded] = new Bin(this.bin_capacity, this.bins[row][max_loaded].day);
            double m = this.bins[row][0].space_left;
            for(int i = 0; i < row; i++)
            {
                if(this.bins[row][i].space_left < m)
                {
                    m = this.bins[row][i].space_left;
                }
            }
            bin_min[row] = m;
            this.addToBinTable(t, row, col);
        }

        else
        {
            this.bins[row][bin].add(t.height);
            this.bins[row][bin].items.add(t);
            bin_min[row] = this.bins[row][bin].space_left;
        }
    }

    int getDifferenceDays(String s1, String s2) throws ParseException
    {
        String dtf = "dd/mm/yyyy";
        Date d1 = new SimpleDateFormat(dtf).parse(s1);
        Date d2 = new SimpleDateFormat(dtf).parse(s2);
        long diff = d2.getTime() - d1.getTime();
        return (int)TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    }

    void orderLocation()
    {
        System.out.print("No. of days old stock is required: ");
        int days_old = sc.nextInt();
        int col = days_old - 1;
        int row = 0;
        // for(int i = warehouse.racks.length - 1; i >= 0; i--)
        // {
        //     if(this.warehouse.racks[i][col].top < 4 && this.warehouse.racks[i][col].top > 0);
        //     {
        //         row = this.warehouse.racks[i][col].top;
        //         break;
        //     }
        // }

        if(days_old > this.day)
        {
            this.source_col = -1;
            this.source_row = -1;
            return;
        }
        for(int i = this.warehouse_row - 1; i >= 0; i--)
        {
            if(warehouse.racks[i][col].top > 0)
            {
                row = i;
                break;
            }
        }
        if(row == 0 && this.warehouse.racks[row][col].top == 0)
        {
            // System.out.println("Stock not Found");
            this.source_col = -1;
            this.source_row = -1;
            sc.close();
            return;
        }
        this.source_row = row;
        this.source_col = col;
        System.out.println("Row = " + row + ", Col = " + col);
        sc.close();
    }

    void printBins()
    {
        System.out.println("BINS TABLE");
        for(int i = 0; i < this.bins.length; i++)
        {
            System.out.print("Day " + (i + 1) + ": ");
            for(int j = 0; j < this.bins[0].length; j++)
            {
                System.out.print(bins[i][j].space_left + "   |    ");
            }
            System.out.println();
        }
    }

    void sourceInIventory()
    {
        this.source_row = this.source_row * 2;
        this.source_col = this.source_col * 2;

        if(this.source_row < this.warehouse.inventory.length - 1 && this.source_col < this.warehouse.inventory[0].length - 1)
        {
            this.source_row = this.source_row + 1;
            this.source_col = this.source_col + 1;
        }
        else if(this.source_row == this.warehouse.inventory.length - 1)
        {
            this.source_row = this.source_row - 1;
        }
        else if(this.source_col == this.warehouse.inventory[0].length - 1)
        {
            this.source_col = this.source_col - 1;
        }
        else
        {
            this.source_row = this.source_row - 1;
            this.source_col = this.source_col - 1;
        }
        
        System.out.println("Source_Row = " + this.source_row);
        System.out.println("Source Column = " + this.source_col);
    }

    public static void main(String[] args)throws Exception
    {
        // Main ob = new Main(args);
        Main ob = new Main();
        // ob.extractInput(args);
        DataSetGen dsg = new DataSetGen();
        dsg.dataSetGen(ob.begin_date, ob.end_date, ob.total_fruits);
        System.out.println("\nProcessing");

        String anim= "|/-\\";
        for (int x =0 ; x <=100 ; x++) 
        {
            String data = "\r" + anim.charAt(x % anim.length()) + " " + x;
            System.out.write(data.getBytes());
            Thread.sleep(20);
        }
        System.out.println("\n");
        ob.extract();
        
        for(int i = 0; i < ob.day; i++)
        {
            System.out.println("Trays used for day " + (i + 1) + " = "+ ob.no_of_tray_used[i]);
        }
        PathAstar astar = new PathAstar(ob.warehouse.inventory);
        System.out.println("\nTotal Bins Used = " + ob.bins_used + "\n");
        ob.printBins();
        ob.warehouse.printRacks();
        System.out.println("\n\nWareshouse Inventory Representation\n");
        ob.warehouse.print();
        ob.orderLocation();
        if(ob.source_row == -1 && ob.source_col == -1)
        {
            System.out.println("Stock isn't Available");
            return;
        }
        ob.sourceInIventory();
        astar.findPath(ob.source_row, ob.source_col, ob.warehouse.inventory.length - 2, ob.warehouse.inventory[0].length - 1);
    }

    void extractInput(String[] args)
    {

    }
}
