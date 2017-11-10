package com.example.administrator.baidumapplay.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
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
    /**
     *  定位client
     */
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
        initMapListener();
        setTrafficLayer();
        initPermission();
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

    /**
     *  @功能描述   初始化地图监听
     *  @入参     无
     *  @返回值    无
     *  @时间  2017/11/9 10:51
     *  @版本     v1.0
     *  @作者     郭晨
     *  @修改原因   无
     */
    private void initMapListener(){
        baiduMap.setOnMapStatusChangeListener(mapStatusChangeListener);
    }

    /**
     *  @功能描述   检查手机是否开启定位权限
     *  @入参     无
     *  @返回值    boolean 是否已经开启了相应权限
     *  @时间  2017/11/9 13:38
     *  @版本     v1.0
     *  @作者     郭晨
     *  @修改原因   无
     *  备注      从Android6.0开始部分权限就需要动态开启
     */
    private void initPermission(){
        if(Build.VERSION.SDK_INT > 23){
            int locationRequest_1 = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
            int locationRequest_2 = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
            if(locationRequest_1 != PackageManager.PERMISSION_GRANTED || locationRequest_2 != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
                return;
            }else{
                getMyLocation();
            }
        }else{
            getMyLocation();
        }
    }

    /**
     *  @功能描述 设置交通状态图层
     *  @入参     无
     *  @返回值    无
     *  @时间  2017/11/9 9:47
     *  @版本     v1.0
     *  @作者     郭晨
     *  @修改原因   无
     */
    private void setTrafficLayer(){
        baiduMap.setTrafficEnabled(true);
        baiduMap.setCustomTrafficColor("#ffba0101", "#fff33131", "#ffff9e19", "#00000000");
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15);
        baiduMap.animateMapStatus(msu);
    }

    /**
     *  @功能描述 设置定位图层
     *  @param bdLocation 位置信息
     *  @返回值   无
     *  @时间  2017/11/9 9:48
     *  @版本     v1.0
     *  @作者     郭晨
     *  @修改原因   无
     */
    private void setLocationLayer(BDLocation bdLocation){
        baiduMap.setMyLocationEnabled(true);
        MyLocationConfiguration.LocationMode myLocationMode = MyLocationConfiguration.LocationMode.NORMAL;

        //设置定位标记样式
        BitmapDescriptor myLocationMarker = BitmapDescriptorFactory.fromResource(R.drawable.marker);
        baiduMap.setMyLocationConfiguration(new MyLocationConfiguration(myLocationMode, true, myLocationMarker));

        //显示定位标记
        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(0)
                .direction(bdLocation.getDirection())
                .latitude(bdLocation.getLatitude())
                .longitude(bdLocation.getLongitude())
                .build();
        baiduMap.setMyLocationData(locData);

        //将地图移动至当前定位位置d
        if(isFirstLoc){
            isFirstLoc = false;
            LatLng latLng = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
            MapStatusUpdate msu = MapStatusUpdateFactory.newLatLngZoom(latLng, 18);
            baiduMap.animateMapStatus(msu);
        }

    }

    /**
     *  @功能描述 获取定位
     *  @入参     无
     *  @返回值    无
     *  @时间  2017/11/9 9:49
     *  @版本     v1.0
     *  @作者     郭晨
     *  @修改原因   无
     */
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case 1000:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                    Log.d("requestPermissonStatus", "成功");
                    getMyLocation();
                }else{
                    Log.d("requestPermissonStatus", "失败");
                    Toast.makeText(MainActivity.this, "应用尚未获取位置权限", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     *  地图状态改变相关监听接口
     */
    BaiduMap.OnMapStatusChangeListener mapStatusChangeListener = new BaiduMap.OnMapStatusChangeListener() {
        @Override
        public void onMapStatusChangeStart(MapStatus mapStatus) {
            Log.d("mapStatus change start", mapStatus.toString());
        }

        @Override
        public void onMapStatusChangeStart(MapStatus mapStatus, int i) {
            Log.d("mapStatus changed by sth", mapStatus.toString());
            Log.d("do sth on BaiduMap", i + "");
        }

        @Override
        public void onMapStatusChange(MapStatus mapStatus) {
            Log.d("mapStatus is changing", mapStatus.toString());
        }

        @Override
        public void onMapStatusChangeFinish(MapStatus mapStatus) {
            Log.d("mapStatus changed finish", mapStatus.toString());
        }
    };
}
