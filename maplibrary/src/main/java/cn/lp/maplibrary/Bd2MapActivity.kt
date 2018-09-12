package cn.lp.maplibrary

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import cn.shequren.map.R
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.SDKInitializer
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import kotlinx.android.synthetic.main.activity_bd2_map.*

class Bd2MapActivity : AppCompatActivity() {


    lateinit var mBaiduMap: BaiduMap
    lateinit var mLocationClient: LocationClient
    var latLng: LatLng? = null
    var iseDitMode: Boolean = false

    var latLngs = arrayListOf<LatLng>();
    var bdPolyline: Overlay? = null
    var mPolylineOptions = PolylineOptions()
    var bdStartMarker1: Overlay? = null
    var bdStartMarker2: Overlay? = null


    /**
     * 临时存放围栏
     */
    var bdPolygonOptions = arrayListOf<PolygonOptions>()

    /**
     * 围栏绘制的引用对象
     */
    var bdDrawPolylineOptionsOverlayList = arrayListOf<Overlay>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_bd2_map)



        intMap()
        intiView()
    }

    private fun intiView() {

        //修改地图模式
        type.setOnClickListener {

            if (mBaiduMap.mapType == BaiduMap.MAP_TYPE_NORMAL) {
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
            } else {
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
            }

        }
        around.setOnClickListener {
            if (around.text.equals("保存")) {
                savPolygonOptions()
            }
            iseDitMode = !iseDitMode;
            if (iseDitMode) {
                latLngs.clear()

                if (mPolylineOptions.points != null && mPolylineOptions.points.size > 1) {
                    mPolylineOptions.points.clear()
                }

                around.setText("保存")
            } else {
                around.setText("添加")
            }
            trajectory.setText("显示围栏")
            closAllPolygonOptions()

        }
        trajectory.setOnClickListener {

            if (trajectory.text.equals("显示围栏")) {
                trajectory.setText("关闭围栏")
                showPolygonOptions()
            } else {
                trajectory.setText("显示围栏")
                closAllPolygonOptions()
            }
        }
    }


    private fun intMap() {
        mBaiduMap = bd_mapView.map;

        //设置地图的模式
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);

        //开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        //设置地图的缩放级别
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(16f));

        mLocationClient = LocationClient(getApplicationContext());     //声明LocationClient类
        /**
         * 设置地图允许所有的手势操作，根据需要可以去设置那些打开那些关闭
         * 使用的类为：java.lang.Object
         *                  com.baidu.mapapi.map.UiSettings
         * 参考Api：http://wiki.lbsyun.baidu.com/cms/androidsdk/doc/v5.2.0/index.html
         */
        mBaiduMap.uiSettings.setAllGesturesEnabled(true)
        //注册监听函数
        var myLocationListener = MyLocation2Listener()
        initLocation()
        mLocationClient.registerLocationListener(myLocationListener);
        //开启定位
        mLocationClient.start();
        //图片点击事件，回到定位点
        mLocationClient.requestLocation();
    }

    //配置定位SDK参数
    private fun initLocation() {
        val option = LocationClientOption()
        option.locationMode = LocationClientOption.LocationMode.Hight_Accuracy//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll")//可选，默认gcj02，设置返回的定位结果坐标系
        option.setScanSpan(1000)//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true)//可选，设置是否需要地址信息，默认不需要
        option.isOpenGps = true//可选，默认false,设置是否使用gps
        option.isLocationNotify = true//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true)//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation
        // .getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true)//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false)
        option.isOpenGps = true // 打开gps

        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false)//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false)//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mLocationClient.locOption = option

        //用户点击地图回调
        mBaiduMap.setOnMapClickListener(object : BaiduMap.OnMapClickListener {
            override fun onMapClick(latLng: LatLng) {

                /**
                 * 根据用户的点击绘制多边形的定点
                 */
                if (iseDitMode) {
                    latLngs.add(latLng)
                    if (latLngs.size > 1) {

                        if (bdPolyline != null) {
                            bdPolyline!!.remove()
                        }
                        mPolylineOptions.points(latLngs).dottedLine(false).color(Color.argb(255, 255, 20, 147)).width(5)
                        bdPolyline = mBaiduMap.addOverlay(mPolylineOptions);
                        bdStartMarker1!!.remove()
                    } else if (latLngs.size > 0) {

                        var markerOption = MarkerOptions()
                        markerOption.position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.express_icon_location_centre_point))
                        bdStartMarker1 = mBaiduMap.addOverlay(markerOption)
                    }
                }

            }

            override fun onMapPoiClick(mapPoi: MapPoi): Boolean {
                return false
            }
        })
    }


    override fun onResume() {
        super.onResume()
        this.bd_mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        this.bd_mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        bd_mapView.onDestroy();
        MapView.setMapCustomEnable(false);
    }

    //实现BDLocationListener接口,BDLocationListener为结果监听接口，异步获取定位结果
    inner class MyLocation2Listener : BDAbstractLocationListener() {

        override fun onReceiveLocation(location: BDLocation) {

            latLng = LatLng(location.latitude, location.longitude)
            setLocationData(latLng!!)
        }

        override fun onConnectHotSpotMessage(s: String?, i: Int) {

        }
    }

    private fun setLocationData(latLng: LatLng) {

        // 构造定位数据
        val locData = MyLocationData.Builder()
                // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(100f).latitude(latLng.latitude)
                .longitude(latLng.longitude).build()
        // 设置定位数据
        mBaiduMap.setMyLocationData(locData)
        val mapStatusUpdate = MapStatusUpdateFactory.newLatLng(latLng)
        mBaiduMap.animateMapStatus(mapStatusUpdate)
        //定义Maker坐标点
    }

    /**
     * 隐藏围栏对象
     */
    private fun closAllPolygonOptions() {
        for (overlay in bdDrawPolylineOptionsOverlayList) {
            overlay.setVisible(false)
        }
    }

    /**
     * 显示围栏对象(高德地图也可以使用该方式显示，)
     */
    private fun showPolygonOptions() {
        for (overlay in bdDrawPolylineOptionsOverlayList) {
            overlay.setVisible(true)
        }

    }


    /**
     * 根据定点生产多边形对象，及可以作为围栏对象
     * 通过com.baidu.mapapi.utils
     *                SpatialRelationUtil
     *              可以判断经纬度是否在电子围栏中
     *              isPolygonContainsPoint（）可以判断对象点位是否在电子围栏中
     */
    private fun savPolygonOptions() {
        if (latLngs.size > 2) {
            //latLngs.add(latLngs.get(0))
            var latLng = arrayListOf<LatLng>()
            latLng = latLngs.clone() as ArrayList<LatLng>;
            var polygonOptions = PolygonOptions().points(latLngs).fillColor(Color.argb(150, 239, 113, 113))
            polygonOptions.visible(false)
            bdPolygonOptions.add(polygonOptions)
            bdDrawPolylineOptionsOverlayList.add(mBaiduMap.addOverlay(polygonOptions))
            if (bdPolyline != null) {
                bdPolyline!!.remove()
            }
            trajectory.setText("显示围栏")

        } else {
            Toast.makeText(this, "至少添加三个点才能建立围栏", Toast.LENGTH_LONG).show()
        }
    }


}
