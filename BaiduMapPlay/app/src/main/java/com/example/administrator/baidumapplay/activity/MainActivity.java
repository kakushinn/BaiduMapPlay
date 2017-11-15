package com.example.administrator.baidumapplay.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import com.baidu.mapapi.map.ArcOptions;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.example.administrator.baidumapplay.R;
import com.example.administrator.baidumapplay.overlayutils.WalkingRouteOverlay;

import java.util.ArrayList;
import java.util.List;

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
    /**
     * 我的位置信息
     */
    private BDLocation myBDLocation = null;
    /**
     * 路线规划
     */
    private RoutePlanSearch mPlanSearch;
    /**
     * 路线规划起始位置
     */
    private PlanNode stNode;
    /**
     * 路线规划终点位置
     */
    private PlanNode enNode;
    /**
     * 规划路线轨迹
     */
    private RouteLine routeLine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initMapListener();
        setTrafficLayer();
        initPermission();
//        addPointMarker();
//        addPointGroupMarker();
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
        if(mPlanSearch != null){
            mPlanSearch.destroy();
        }
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mapView.onDestroy();
        super.onDestroy();
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
        baiduMap.setOnMapClickListener(mapClickListener);
//        baiduMap.setOnMarkerClickListener(markerClickListener);
        baiduMap.setOnMyLocationClickListener(myLocationClickListener);
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
            myBDLocation = bdLocation;
            LatLng latLng = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
            MapStatusUpdate msu = MapStatusUpdateFactory.newLatLngZoom(latLng, 18);
            baiduMap.animateMapStatus(msu);
//            planWalkRoute();
//            addPolyLineMarker();
//            addPolyLineMarkerByPart();
//            addArcMarker();
            addCircleMarker();
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

    /**
     *  @功能描述 权限请求结果回调函数
     *  @param requestCode 请求码
     *  @param permissions 请求权限数组
     *  @param grantResults  请求结果数组
     *  @返回值    无
     *  @时间  2017/11/13 13:35
     *  @版本  v1.0
     *  @作者  郭晨
     *  @修改原因   Android6.0以上手机开启时无法自动定位
     */
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

    /**
    *  地图单击事件监听接口
    */
    BaiduMap.OnMapClickListener mapClickListener = new BaiduMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            Log.d("mapClickLat", latLng.latitude + "");
            Log.d("mapClickLng", latLng.longitude + "");
        }

        @Override
        public boolean onMapPoiClick(MapPoi mapPoi) {
            Log.d("mapClickMapPoiName", mapPoi.getName());
            return false;
        }
    };

    /**
     *  地图单击事件监听接口
     */
    BaiduMap.OnMarkerClickListener markerClickListener = new BaiduMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            Log.d("myLocationMarker", marker.getTitle());
            return false;
        }
    };

    /**
     *  地图定位图标点击事件监听接口
     */
    BaiduMap.OnMyLocationClickListener myLocationClickListener = new BaiduMap.OnMyLocationClickListener() {
        @Override
        public boolean onMyLocationClick() {
            Log.d("myLocationAddr", myBDLocation.getAddrStr());
            return false;
        }
    };

    /**
     *  @功能描述   规划步行路线
     *  @入参     无
     *  @返回值    无
     *  @时间  2017/11/14 15:04
     *  @版本     v1.0
     *  @作者     郭晨
     *  @修改原因   无
     */
    private void planWalkRoute(){
        mPlanSearch = RoutePlanSearch.newInstance();
        mPlanSearch.setOnGetRoutePlanResultListener(onGetRoutePlanResultListener);
        LatLng stLatLng = new LatLng(myBDLocation.getLatitude(), myBDLocation.getLongitude());
        stNode = PlanNode.withLocation(stLatLng);
        LatLng enLatLng = new LatLng(32.01521426116813, 118.77236450871254);
        enNode = PlanNode.withLocation(enLatLng);
        mPlanSearch.walkingSearch(new WalkingRoutePlanOption().from(stNode).to(enNode));
    }

    /**
     *  线路规划检索监听接口
     */
    OnGetRoutePlanResultListener onGetRoutePlanResultListener = new OnGetRoutePlanResultListener() {
        @Override
        public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
            if(walkingRouteResult == null || walkingRouteResult.error != SearchResult.ERRORNO.NO_ERROR){
                Toast.makeText(MainActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
            }
            if(walkingRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR){
                // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                // result.getSuggestAddrInfo()
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("提示");
                builder.setMessage("检索地址有歧义，请重新设置。\n可通过getSuggestAddrInfo()接口获得建议查询信息");
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                return;
            }
            if(walkingRouteResult.error == SearchResult.ERRORNO.NO_ERROR){
//                routeLine = walkingRouteResult.getRouteLines().get(0);
                WalkingRouteOverlay walkingRouteOverlay = new WalkingRouteOverlay(baiduMap);
                baiduMap.setOnMarkerClickListener(walkingRouteOverlay);
                walkingRouteOverlay.setData(walkingRouteResult.getRouteLines().get(0));
                walkingRouteOverlay.addToMap();
                walkingRouteOverlay.zoomToSpan();
            }
        }

        @Override
        public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

        }

        @Override
        public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

        }

        @Override
        public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {

        }

        @Override
        public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

        }

        @Override
        public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

        }
    };

    /**
     *  @功能描述   添加单个点标记
     *  @入参     无
     *  @返回值    无
     *  @时间  2017/11/14 15:56
     *  @版本     v1.0
     *  @作者     郭晨
     *  @修改原因   无
     */
    private void addPointMarker(){
        LatLng point = new LatLng(32.01521426116813, 118.77236450871254);
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.marker);
        OverlayOptions overlayOptions = new MarkerOptions().position(point).icon(bitmap);
        baiduMap.addOverlay(overlayOptions);
    }

    /**
     *  @功能描述   批量添加点标记
     *  @入参     无
     *  @返回值    无
     *  @时间  2017/11/14 16:04
     *  @版本     v1.0
     *  @作者     郭晨
     *  @修改原因   无
     */
    private void addPointGroupMarker(){
        List<OverlayOptions> overLayOptionsList = new ArrayList<>();
        LatLng point1 = new LatLng(32.01521426116813, 118.77236450871254);
        BitmapDescriptor bitmap1 = BitmapDescriptorFactory.fromResource(R.drawable.marker);
        OverlayOptions overlayOptions1 = new MarkerOptions().position(point1).icon(bitmap1);
        LatLng point2 = new LatLng(32.03654377772714, 118.74477754650111);
        BitmapDescriptor bitmap2 = BitmapDescriptorFactory.fromResource(R.drawable.marker);
        OverlayOptions overlayOptions2 = new MarkerOptions().position(point2).icon(bitmap2);
        overLayOptionsList.add(overlayOptions1);
        overLayOptionsList.add(overlayOptions2);
        baiduMap.addOverlays(overLayOptionsList);
    }

    /**
     *  @功能描述   绘制折线
     *  @入参     无
     *  @返回值    无
     *  @时间  2017/11/14 16:30
     *  @版本     v1.0
     *  @作者     郭晨
     *  @修改原因   无
     */
    private void addPolyLineMarker(){
        LatLng p1 = new LatLng(myBDLocation.getLatitude(), myBDLocation.getLongitude());
        LatLng p2 = new LatLng(32.03654377772714, 118.74477754650111);
        LatLng p3 = new LatLng(32.01521426116813, 118.77236450871254);
        List<LatLng> pointList = new ArrayList<>();
        pointList.add(p1);
        pointList.add(p2);
        pointList.add(p3);
        OverlayOptions ooPolyLine = new PolylineOptions()
                .width(10)
                    .color(0xAAFF0000)
                        .points(pointList);
        Polyline mPolyline = (Polyline) baiduMap.addOverlay(ooPolyLine);
        //是否需要绘制虚线（在绘制普通折线的基础上，设置如下代码即可）
//        mPolyline.setDottedLine(true);
    }

    /**
     *  @功能描述   分段绘制折线（每段颜色不同）
     *  @入参     无
     *  @返回值    无
     *  @时间  2017/11/14 16:44
     *  @版本     v1.0
     *  @作者     郭晨
     *  @修改原因   无
     */
    private void addPolyLineMarkerByPart(){
        //构造折线点坐标
        List<LatLng> pointList = new ArrayList<>();
        pointList.add(new LatLng(myBDLocation.getLatitude(), myBDLocation.getLongitude()));
        pointList.add(new LatLng(32.03654377772714, 118.74477754650111));
        pointList.add(new LatLng(32.01521426116813, 118.77236450871254));

        //构建分段颜色索引数组
        List<Integer> colorList = new ArrayList<>();
        colorList.add(Integer.valueOf(Color.BLUE));
        colorList.add(Integer.valueOf(Color.GREEN));

        OverlayOptions ooPolyLine = new PolylineOptions()
                                        .width(10)
                                            .colorsValues(colorList)
                                                .points(pointList);

        Polyline  mPolyline = (Polyline) baiduMap.addOverlay(ooPolyLine);
    }

    /**
     *  @功能描述   绘制弧线
     *  @入参     无
     *  @返回值    无
     *  @时间  2017/11/14 17:35
     *  @版本     v1.0
     *  @作者     郭晨
     *  @修改原因   无
     */
    private void addArcMarker(){
        LatLng p1 = new LatLng(myBDLocation.getLatitude(), myBDLocation.getLongitude());
        LatLng p2 = new LatLng(32.03654377772714, 118.74477754650111);
        LatLng p3 = new LatLng(32.01521426116813, 118.77236450871254);
        OverlayOptions ooArc = new ArcOptions()
                                    .color(0xAA00FF00)
                                        .width(8)
                                            .points(p1, p2, p3);
        baiduMap.addOverlay(ooArc);
    }

    private void addCircleMarker(){
        LatLng center = new LatLng(myBDLocation.getLatitude(), myBDLocation.getLongitude());
        OverlayOptions ooCircle = new CircleOptions()
                                        .fillColor(0x000000FF)
                                            .center(center)
                                                .stroke(new Stroke(5, 0xAA000000))
                                                    .radius(1400);
        baiduMap.addOverlay(ooCircle);
    }
}
