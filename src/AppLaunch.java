import javax.swing.*;

public class AppLaunch {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                new WeatheGui().setVisible(true);
//                System.out.println(WeatherApp.getLocationData("Tokyo"));
            }

        });
    }
}