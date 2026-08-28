package com.hospitaltelemetry.telemetry;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MeasurementTypeTest {
    
    @Test
    void shouldReturnCorrectUnitForEachMeasurementType(){
        assertEquals(MeasurementUnit.BPM, MeasurementType.HEART_RATE.getUnit());
        assertEquals(MeasurementUnit.PERCENT, MeasurementType.OXYGEN_SATURATION.getUnit());
        assertEquals(MeasurementUnit.BREATHS_PER_MINUTE, MeasurementType.RESPIRATORY_RATE.getUnit());
        assertEquals(MeasurementUnit.MMHG, MeasurementType.SYSTOLIC_BLOOD_PRESSURE.getUnit());
        assertEquals(MeasurementUnit.MMHG, MeasurementType.DIASTOLIC_BLOOD_PRESSURE.getUnit());
    }
    @Test
    void shouldContainOnlySupportedMeasurementTypes(){
        assertEquals(6, MeasurementType.values().length);
    }
    @Test
    void shouldHaveUnitForEveryMeasurementType(){
        for (MeasurementType type : MeasurementType.values()){
            assertNotNull(type.getUnit());
        }
    }
}
