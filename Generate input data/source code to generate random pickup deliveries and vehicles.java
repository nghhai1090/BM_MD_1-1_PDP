
import java.util.Random;

public class App 
{
    public static void main( String[] args )
    {
        generateTransports(1);
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        generateVehicles(9);

    }
    public static void generateTransports(int number) {
        for(int i = 0 ; i < number ; i++) {
            Random rand = new Random();
            int from = rand.nextInt(17);
            int to = from;
            while(to==from) {
                to = rand.nextInt(17);
            }
            int timePick = rand.nextInt(10);
            int timeDel = rand.nextInt(10);
            int amount = rand.nextInt(19);
            System.out.println(from+","+to+","+amount+","+timePick+","+timeDel);
        }
    }

    public static void generateVehicles(int number) {
        for(int i = 0 ; i < number ; i++) {
            Random rand = new Random();
            int vehicleType = rand.nextInt(2);
            int depot = rand.nextInt(17);
            if(vehicleType==0) {
                System.out.println(depot+","+90+","+18);
            }
            else{
                System.out.println(depot+","+80+","+40);
            }
        }
    }

}
