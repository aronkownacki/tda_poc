package pl.edu.uj.student.kownacki.aron.tda.batch.utils;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.Test;

import pl.edu.uj.student.kownacki.aron.tda.batch.model.Country;
import twitter4j.HashtagEntity;
import twitter4j.Status;

/**
 * Created by Aron Kownacki on 20.06.2018.
 */
public class TwitterHelperTest {
    @Test
    public void shouldExtractCountriesFromText() throws Exception {

        Status status = mock(Status.class);
        when(status.getText()).thenReturn("abc_polexit_abc");
        when(status.getHashtagEntities()).thenReturn(new HashtagEntity[]{});
        Set<Country> countries = TwitterHelper.extractCountries(status);
        assertTrue(countries.contains(Country.POLAND));
    }

    private static class HashtagEntityImpl implements HashtagEntity {

        @Override
        public String getText() {
            return null;
        }

        @Override
        public int getStart() {
            return 0;
        }

        @Override
        public int getEnd() {
            return 0;
        }
    }
}