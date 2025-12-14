package com.astral.universe;

import java.util.List;

/**
 * Interface for star system data.
 */
public interface IStarsystem {
    String getName();
    List<IPlanet> getPlanets();
}