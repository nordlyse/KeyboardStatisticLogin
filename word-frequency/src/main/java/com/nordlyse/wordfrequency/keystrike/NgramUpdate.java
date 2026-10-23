package com.nordlyse.wordfrequency.keystrike;

record NgramUpdate(int orderSize, String sequence, double[] stepMillis, boolean sample, boolean erroneous) {

    NgramUpdate {
        stepMillis = stepMillis == null ? new double[0] : stepMillis.clone();
    }

    static NgramUpdate sample(int orderSize, String sequence, double[] stepMillis, boolean erroneous) {
        return new NgramUpdate(orderSize, sequence, stepMillis, true, erroneous);
    }

    static NgramUpdate error(int orderSize, String sequence) {
        return new NgramUpdate(orderSize, sequence, new double[0], false, true);
    }
}
