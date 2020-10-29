package test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import unsw.gloriaromanus.*;

public class FactionTest {
    @Test
    public void initilisationTest() {
        Faction test = new Faction("Rome");
        assert(test.getName().equals("Rome"));
    }

    @Test
    public void addProvinceTest() throws IOException {
        Faction test = new Faction("Rome");
        Province province = new Province("Lusitania", test);
        test.addProvince(province);
        assert(test.getProvince("Lusitania") == province);

    }
}
