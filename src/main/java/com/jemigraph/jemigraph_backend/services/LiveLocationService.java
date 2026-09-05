package com.jemigraph.jemigraph_backend.services;

import com.jemigraph.jemigraph_backend.DTO.LiveLocationUpdate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface LiveLocationService {

    List<Object> getNearbyPhotographers(double lat, double lng);
    void updateLiveLocation(LiveLocationUpdate update);
}
