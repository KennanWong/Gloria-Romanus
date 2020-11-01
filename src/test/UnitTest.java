package test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import unsw.gloriaromanus.*;

public class UnitTest {
    @Test
    public void blahTest() {
        assertEquals("a", "a");
    }

    @Test
    public void checkConfigSwordsmen() throws IOException {
        Unit swordsmen = new Unit("Swordsmen", 10);
        assert(swordsmen.getNumTroops() == 10);
        assert(swordsmen.getCategory().equals("Infantry"));
        assert(swordsmen.getMovementPoints() == 10);
    }

    @Test
    public void checkConfigChariot() throws IOException {
        Unit chariot = new Unit("Chariot", 10);
        assert(chariot.getNumTroops() == 10);
        assert(chariot.getCategory().equals("Cavalry"));
        assert(chariot.getMovementPoints() == 15);
    }

    @Test
    public void checkConfigCamelArcher() throws IOException {
        Unit camelArcher = new Unit("Camel Archer", 10);
        assert(camelArcher.getNumTroops() == 10);
        assert(camelArcher.getCategory().equals("Artillery"));
        assert(camelArcher.getMovementPoints() == 4);
    }
}

