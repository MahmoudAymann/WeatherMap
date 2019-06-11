package com.maymanm.weathermap.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.maymanm.weathermap.models.directions.DirectionsModel;
import com.maymanm.weathermap.repository.DirectionRepository;

/**
 * Created by MahmoudAyman on 6/11/2019.
 **/
public class DirectionsViweModel extends ViewModel {

    public LiveData<DirectionsModel> getDirectionsLiveData(String origin, String dest, String key){
        return DirectionRepository.getInstance().getDirection(origin, dest, key);
    }

    public LiveData<String[]> getPolyLineLiveData(DirectionsModel directionsModel){
        return DirectionRepository.getInstance().getPolyLine(directionsModel);
    }



}
