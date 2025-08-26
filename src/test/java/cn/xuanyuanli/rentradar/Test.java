package cn.xuanyuanli.rentradar;

import cn.xuanyuanli.rentradar.model.Subway;
import cn.xuanyuanli.rentradar.service.SubwayDataService;
import cn.xuanyuanli.rentradar.service.ServiceContainer;

import java.util.List;

public class Test {
    public static void main(String[] args) throws Exception {
        ServiceContainer container = new ServiceContainer();
        SubwayDataService subwayDataService = new SubwayDataService(container.getZiroomCrawler(), container.getLocationService());

        List<Subway> stationsData = subwayDataService.getStationsData();
        subwayDataService.getLocationData(stationsData);
        subwayDataService.getPriceData(stationsData);

        container.shutdown();
    }
}
