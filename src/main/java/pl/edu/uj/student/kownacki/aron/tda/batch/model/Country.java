package pl.edu.uj.student.kownacki.aron.tda.batch.model;

import static com.google.common.collect.Sets.newHashSet;

import java.util.Arrays;
import java.util.Set;

/**
 * Created by Aron Kownacki on 14.08.2017.
 */
public enum Country {


    AUSTRIA(newHashSet("auxit", "auexit", "ausexit", "autrexit", "austrexit", "oustria", "outstria")),
    BELGIUM(newHashSet("bexit", "belxit", "bexlexit", "byegium", "belgexit")),
    BULGARIA(newHashSet("bulgexit")),
    CROATIA(newHashSet("croexit", "krexit")),
    CYPRUS(newHashSet("cyprexit")),
    CZECH_REPUBLIC(newHashSet("czekout", "czechxit", "czexit", "visegradexit")),
    DENMARK(newHashSet("daxit", "denexit", "danskexit")),
    ESTONIA(newHashSet("esxit", "estxit", "estexit")),
    FINLAND(newHashSet("finexit", "finxit")),
    FRANCE(newHashSet("fexit", "frexit", "fruckoff")),
    GERMANY(newHashSet("dexit", "germexit", "germanexit")),
    GREECE(newHashSet("grexit", "grxit")),
    HUNGARY(newHashSet("huexit", "hungexit", "hunexit", "visegradexit")),
    IRELAND(newHashSet("irexit", "irlexit")),
    ITALY(newHashSet("italexit", "itexit", "italeave")),
    LATVIA(newHashSet("letxit", "latvexit")),
    LITHUANIA(newHashSet("litexit", "litxit", "lithuexit")),
    LUXEMBOURG(newHashSet("luxexit")),
    MALTA(newHashSet("malexit", "maltaexit")),
    NETHERLANDS(newHashSet("nethexit", "hollexit", "nexit", "netherexit")),
    POLAND(newHashSet("polexit", "plexit", "polout", "visegradexit")),
    PORTUGAL(newHashSet("porxit", "pexit", "departugal")),
    ROMANIA(newHashSet("romexit", "roexit")),
    SLOVAKIA(newHashSet("slexit", "sloxit", "slovxit", "visegradexit")),
    SLOVENIA(newHashSet("slovexit")),
    SPAIN(newHashSet("espxit", "spaxit", "spexit")),
    SWEDEN(newHashSet("swexit")),
    UNITED_KINGDOM(newHashSet("ukexit", "brexit")),
    EU(newHashSet("euexit", "uexit", "leaveeu"));

    private final Set<String> hashtags;

    public Set<String> getHashtags() {
        return hashtags;
    }

    Country(Set<String> hashtags) {
        this.hashtags = hashtags;
    }

    public static String[] getAllHashtags() {
        return Arrays.stream(Country.values()).flatMap(c -> c.getHashtags().stream()).toArray(String[]::new);
    }
}
