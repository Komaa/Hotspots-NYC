# NYCab Project

The **NYCab Project** (New York Cab) is a student project by SNET. The goal is to analyse the given cab data set using
data science methods finding hostspots in the city. Another part of the project is predicting new hotspots given the
existing data for training and weather data as well as facebook events.

## Use Case

## Data Sets

Different data sets are used for this project. Some of them are located in `data/` others need to be downloaded
manually.

### NYCab

The most important data set is the trip data set which includes the cab data. The data set is not included in this
repository. You need to download the trip data set from [GitHub](http://www.andresmh.com/nyctaxitrips/).

The downloaded CSV files need to be placed to `data/trips/`.

### Weather

The weather data for NYC from 2013 are downloaded from [WeatherUnderground.com](http://weatherunderground.com) and a
combined CSV file is save in `data/weather.csv`.

### Events

Events data are issued from Facebook using their API. The data is also saved in `data/`.
