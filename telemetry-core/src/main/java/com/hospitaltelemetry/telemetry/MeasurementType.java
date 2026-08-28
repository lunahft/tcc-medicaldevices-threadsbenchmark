package com.hospitaltelemetry.telemetry;

public enum MeasurementType {
    HEART_RATE(MeasurementUnit.BPM),
    OXYGEN_SATURATION(MeasurementUnit.PERCENT),
    BODY_TEMPERATURE(MeasurementUnit.CELSIUS),
    RESPIRATORY_RATE(MeasurementUnit.BREATHS_PER_MINUTE),
    SYSTOLIC_BLOOD_PRESSURE(MeasurementUnit.MMHG),
    DIASTOLIC_BLOOD_PRESSURE(MeasurementUnit.MMHG);

    private final MeasurementUnit unit;

    MeasurementType(MeasurementUnit unit){
        this.unit = unit;
    }

    public MeasurementUnit getUnit(){
        return unit;
    }
}
