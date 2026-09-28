import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class WeatheGui extends JFrame
{   private JSONObject weatherData;
    public WeatheGui() {
        super("Weather App");
        //gui to end program
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //set size of gui
        setSize(450, 650);

        //load gui
        setLocationRelativeTo(null);

        //layout manager null to manually position
        setLayout(null);
        //prevent resize
        setResizable(false);


        addGuiComponents();
    }

    private void addGuiComponents() {

        JTextField searchTextField = new JTextField();

        // Location and size
        searchTextField.setBounds(15, 15, 351, 45);

        // Font
        searchTextField.setFont(new Font("Dialog", Font.PLAIN, 24));

        add(searchTextField);


        //weather image
        JLabel weatherConditionImage=new JLabel(loadImage("/assets/cloudy.png"));
        weatherConditionImage.setBounds(0,125,450,217);
        add(weatherConditionImage);

        //temperature
        JLabel temperatureText=new JLabel("10 C");
        temperatureText.setBounds(0,350,450,54);
        temperatureText.setFont(new Font("Dialog",Font.BOLD,48));

        //text at center
        temperatureText.setHorizontalAlignment(SwingConstants.CENTER);
        add(temperatureText);
        //weather condition description

        JLabel weatherConditionDesc = new JLabel("Cloudy");
        weatherConditionDesc.setBounds(0,405,450,36);
        weatherConditionDesc.setFont(new Font("Dialog",Font.PLAIN,32));
        weatherConditionDesc.setHorizontalAlignment(SwingConstants.CENTER);
        add(weatherConditionDesc);

        //Humidity
        JLabel humidityImage=new JLabel(loadImage("/assets/humidity.png"));
        humidityImage.setBounds(15,500,74,66);
        add(humidityImage);

        //Humidity text
        JLabel humidityText=new JLabel("<html><b>Humidity</b>100%</html>");
        humidityText.setBounds(90,500,85,55);
        humidityText.setFont(new Font("Dialog",Font.PLAIN,16));
        add(humidityText);

        //wind Speed
        JLabel windspeedImage=new JLabel(loadImage("/assets/windspeed.png"));
        windspeedImage.setBounds(220,500,74,66);
        add(windspeedImage);
        //wind speed txt
        JLabel windspeedText=new JLabel("<html><b>Windspeed</b> 15km/h<br></html>");
        windspeedText.setBounds(310,500,125,55);
        windspeedText.setFont(new Font("Dialog",Font.PLAIN,16));
        add(windspeedText);

        //search button
        JButton searchButton=new JButton(loadImage("/assets/search.png"));

        //changing cursor to hand cursor
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.setBounds(375,13,47,45);
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //to get location from user
                String userInput = searchTextField.getText();
                //validate input:- remove whitespace to ensure non-empty text
                if(userInput.replaceAll("\\s","").length()<=0){
                    return;
                }
                //retrive weather data
                weatherData = WeatherApp.getWeatherData(userInput);

                //gui update
                //update weather image
                String weatherCondition = (String)  weatherData.get("weather_condition");

                //update weather image depend on condition
                switch(weatherCondition){
                    case "Clear":
                        weatherConditionImage.setIcon(loadImage("/assets/clear.png"));
                        break;
                    case "Cloudy":
                        weatherConditionImage.setIcon(loadImage("/assets/cloudy.png"));
                        break;
                    case "Rain":
                        weatherConditionImage.setIcon(loadImage("/assets/rain.png"));
                        break;
                    case "Snow":
                        weatherConditionImage.setIcon(loadImage("/assets/snow.png"));
                        break;
                }

                //update temperarture text
                double temperature =(double) weatherData.get("temperature");
                temperatureText.setText(temperature+"C");

                //update weather condition text
                weatherConditionDesc.setText(weatherCondition);

                //update humidity text
                long humidity = (long) weatherData.get("humidity");
                humidityText.setText("<html><b>Humidity</b><br>" + humidity + "%</html>");

                //update windspeed
                double windspeed = ((Number) weatherData.get("windspeed")).doubleValue();

                windspeedText.setText("<html><b>Windspeed</b><br>" + windspeed + " km/h</html>");
            }
        });
        add(searchButton);
    }
    // create image in gui component
    private ImageIcon loadImage(String resourcepath) {
        try {
            java.net.URL imageUrl = getClass().getResource(resourcepath);

            if (imageUrl == null) {
                System.out.println("Image not found: " + resourcepath);
                return null;
            }

            return new ImageIcon(imageUrl);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }}