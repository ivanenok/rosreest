package ru.ivanenok;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import ru.ivanenok.model.MapInfo;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by ivanenok on 2/11/16.
 */
public class Loader {
    public List<MapInfo.ItemInfo> loadByRegions(List<String> regions) throws IOException {
        ArrayList<MapInfo.ItemInfo> result = new ArrayList<>();
        List<MapInfo.ItemInfo> infos = loadInfo(buildSearchByRegionUrl(regions.get(0)));
        List<String> areas = resolveCadNumbers("CAD_NUM", infos);
        result.addAll(loadByArea(areas));
        return result;
    }

    private static List<String> resolveCadNumbers(String key, List<MapInfo.ItemInfo> infos) {
        return infos.stream().map(MapInfo.ItemInfo::getAttributes).map(map -> map.get(key)).collect(Collectors.toList());
    }

    private static String buildSearchByRegionUrl(String region) {
        return "http://maps.rosreestr.ru/arcgis/rest/services/Cadastre/CadastreSelected/MapServer/3/query?f=json&where=PKK_ID%20like%20%27" + region + "%25%27&returnGeometry=true&spatialRel=esriSpatialRelIntersects&outFields=*";
    }

    public List<MapInfo.ItemInfo> loadByArea(List<String> areas) throws IOException {
        ArrayList<MapInfo.ItemInfo> result = new ArrayList<>();
        List<MapInfo.ItemInfo> infos = areas.stream().flatMap(s -> loadInfo(buildSearchByAreaUrl(s)).stream()).collect(Collectors.toList());
        List<String> kvartals = resolveCadNumbers("KVARTAL_ID", infos);
        System.out.println("kvartals count: " + kvartals.size() + " in areas count: " + areas.size());
        result.addAll(loadByKvartals(kvartals));
        return  result;
    }

    private List<MapInfo.ItemInfo> loadByKvartals(List<String> kvartals) {
        ArrayList<MapInfo.ItemInfo> result = new ArrayList<>();
        List<MapInfo.ItemInfo> infos = kvartals.stream().flatMap(s -> loadInfo(buildSearchByKvartalUrl(s)).stream()).collect(Collectors.toList());
        List<String> places = resolveCadNumbers("CAD_NUM", infos);

        System.out.println("places count : " + places.size() + " in kvartals count: " + kvartals.size());
//        result.addAll(loadByKvartals(kvartals));
        return  result;
    }

    private static String buildSearchByAreaUrl(String area) {
        String code = URLEncoder.encode("CAD_NUM like '" + area + ":%'");
        return "http://maps.rosreestr.ru/arcgis/rest/services/Cadastre/CadastreSelected/MapServer/2/query?f=json&where=" + code + "&returnGeometry=false&spatialRel=esriSpatialRelIntersects&outFields=*";
    }

    private static String buildSearchByKvartalUrl(String kvartal) {
        String code = URLEncoder.encode(kvartal + ":%'");
        return "http://maps.rosreestr.ru/arcgis/rest/services/Cadastre/CadastreSelected/MapServer/exts/GKNServiceExtension/online/parcel/find?cadNum=" + code + "&onlyAttributes=true&returnGeometry=true&f=json&onlyIds=true";
    }


    public List<MapInfo.ItemInfo> loadbyCadastrNumber(String... area) throws IOException {
        ArrayList<MapInfo.ItemInfo> result = new ArrayList<>();

        String criteria = "PKK_ID%20like%20%2739%25%27";

//        /* by item*/
//        String url = "http://maps.rosreestr.ru/arcgis/rest/services/Cadastre/CadastreSelected/MapServer/exts/GKNServiceExtension/online/parcel/find?cadNum=39%3A1%3A10101%3A60&onlyAttributes=false&returnGeometry=true&f=json";

        /* by cad number*/
        String url = "http://maps.rosreestr.ru/arcgis/rest/services/Cadastre/CadastreSelected/MapServer/2/query?f=json&where=CAD_NUM%20like%20%2739%3A01%3A%25%27&returnGeometry=false&spatialRel=esriSpatialRelIntersects&outFields=*";

        return loadInfo(url);
    }

    public List<MapInfo.ItemInfo> loadbyItem(String... items) throws IOException {
        ArrayList<MapInfo.ItemInfo> result = new ArrayList<>();
        String criteria = "PKK_ID%20like%20%2739%25%27";

        /* by item*/
        String url = "http://maps.rosreestr.ru/arcgis/rest/services/Cadastre/CadastreSelected/MapServer/exts/GKNServiceExtension/online/parcel/find?cadNum=39%3A1%3A10101%3A60&onlyAttributes=false&returnGeometry=true&f=json";

        return loadInfo(url);
    }

    private List<MapInfo.ItemInfo> loadInfo(String url){
        ArrayList<MapInfo.ItemInfo> result = new ArrayList<>();
        MapInfo mapInfo;
        try {
            String content = IOUtils.toString(new URL(url));
            mapInfo = new ObjectMapper().readValue(content, MapInfo.class);
            result.addAll(mapInfo.getFeatures());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
