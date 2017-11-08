package com.example.administrator.baidumapplay.activity;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.example.administrator.baidumapplay.R;

public class MainActivity extends AppCompatActivity {

    private TextureMapView mapView;
    private BaiduMap baiduMap;
    private LocationClient client;
    /**
     * 是否是第一次定位
     */
    private boolean isFirstLoc = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        getMyLocation();
        setTrafficLayer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mapView.onDestroy();
    }

    /**
     *  @功能描述 初始化页面
     *  @入参     无
     *  @返回值    无
     *  @时间  2017/11/7 14:28
     *  @版本    v1.0
     *  @作者    郭晨
     *  @修改原因   无
     */
    private void initView(){
        mapView = (TextureMapView) findViewById(R.id.mapview_baidu);
        baiduMap = mapView.getMap();
        baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
    }

    private void setTrafficLayer(){
        baiduMap.setTrafficEnabled(true);
        baiduMap.setCustomTrafficColor("#ffba0101", "#fff33131", "#ffff9e19", "#00000000");
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15);
        baiduMap.animateMapStatus(msu);
    }

    private void setLocationLayer(BDLocation bdLocation){
        baiduMap.setMyLocationEnabled(true);
        MyLocationConfiguration.LocationMode myLocationMode = MyLocationConfiguration.LocationMode.NORMAL;
        BitmapDescriptor myLocationMarker = BitmapDescriptorFactory.fromResource(R.drawable.marker);
        int accuracyCircleFillColor = 0xFFFFFFFF;//自定义精度圈填充颜色
        int accuracyCircleStrokeColor = 0xFFFFFFFF;//自定义精度圈边框颜色
        baiduMap.setMyLocationConfiguration(new MyLocationConfiguration(myLocationMode, true, myLocationMarker));
        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(0)
                .direction(bdLocation.getDirection())
                .latitude(bdLocation.getLatitude())
                .longitude(bdLocation.getLongitude())
                .build();
        baiduMap.setMyLocationData(locData);
        if(isFirstLoc){
            isFirstLoc = false;
            LatLng latLng = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
            MapStatusUpdate msu = MapStatusUpdateFactory.newLatLngZoom(latLng, 18);
            baiduMap.animateMapStatus(msu);
        }

    }

    private void getMyLocation(){
        client = new LocationClient(getApplicationContext());
        client.registerLocationListener(new BDAbstractLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                setLocationLayer(bdLocation);
            }
        });
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setScanSpan(1000);
        option.setOpenGps(true);
        option.setIsNeedAddress(true);
        client.setLocOption(option);
        client.start();
    }
}
