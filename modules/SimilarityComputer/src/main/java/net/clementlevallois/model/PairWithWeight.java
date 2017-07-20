/*
 * Copyright 2017 Clément Levallois
 * http://wwww.clementlevallois.net
 */
package net.clementlevallois.model;

import java.util.Objects;

/**
 *
 * @author LEVALLOIS
 */
public class PairWithWeight {
    
    private Pair pair;
    private double weight;

    public Pair getPair() {
        return pair;
    }

    public void setPair(Pair pair) {
        this.pair = pair;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.pair);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PairWithWeight other = (PairWithWeight) obj;
        return Objects.equals(this.pair, other.pair);
    }
    
    
    
}
