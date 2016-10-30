//
//  RCTAlipay.m
//  RCTAlipay
//
//  Created by DengYun on 4/21/16.
//  Copyright © 2016 DengYun. All rights reserved.
//

#import "RCTBDMap.h"
#import "RCTEventDispatcher.h"

#import <BaiduMapAPI_Base/BMKBaseComponent.h>//引入base相关所有的头文件
#import <BaiduMapAPI_Map/BMKMapComponent.h>//引入地图功能所有的头文件
#import <BaiduMapAPI_Location/BMKLocationComponent.h>//引入定位功能所有的头文件
#import <BaiduMapAPI_Utils/BMKUtilsComponent.h>//引入计算工具所有的头文件

static BMKMapManager* _mapManager;

@implementation RCTBDMap {
    BMKLocationService* _locService;
}

RCT_EXPORT_MODULE(RCTBaiduMap);

@synthesize bridge = _bridge;

- (NSDictionary *)constantsToExport
{
    return @{};
};

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

- (instancetype)init
{
    self = [super init];
    
    // 初始化ApiKey
    if (_mapManager == nil) {
        NSString *apiKey = [[[NSBundle mainBundle] infoDictionary] valueForKey:@"BDMapApiKey"];
        _mapManager = [[BMKMapManager alloc] init];
        BOOL ret = [_mapManager start:apiKey  generalDelegate:nil];
    }
    
    // 初始化定位服务
    _locService = [[BMKLocationService alloc]init];
    _locService.delegate = self;
    
    return self;
}

- (void)dealloc
{
}

RCT_EXPORT_METHOD(getDistance:(BMKMapPoint)point1 withPoint:(BMKMapPoint)point2 resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)
{
    resolve(@(BMKMetersBetweenMapPoints(point1, point2)));
}

RCT_EXPORT_METHOD(setOptions:(NSDictionary*)options resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)
{
    resolve(nil);
}

RCT_EXPORT_METHOD(startWatch)
{
    [_locService startUserLocationService];
}

RCT_EXPORT_METHOD(stopWatch)
{
    [_locService stopUserLocationService];
}

//处理位置坐标更新
- (void)didUpdateBMKUserLocation:(BMKUserLocation *)userLocation
{
    [_bridge.eventDispatcher sendAppEventWithName:@"BDMapLocation" body:[self.class _convertLocation:userLocation]];
}

- (void)didFailToLocateUserWithError:(NSError *)error
{
    [_bridge.eventDispatcher sendAppEventWithName:@"BDMapLocationError" body:@{@"code": @(error.code)}];
}

+ (NSDictionary *)_convertLocation:(BMKUserLocation *)userLocation
{
    NSMutableDictionary* ret = [NSMutableDictionary dictionary];
    if (userLocation.location != nil) {
        [ret setObject:@(userLocation.location.coordinate.latitude) forKey:@"latitude" ];
        [ret setObject:@(userLocation.location.coordinate.longitude) forKey:@"longitude" ];
        [ret setObject:@(userLocation.location.altitude) forKey:@"altitude" ];
        [ret setObject:@(userLocation.location.speed) forKey:@"speed" ];
    }
    if (userLocation.heading != nil) {
    }
    if (userLocation.title != nil) {
        [ret setObject:userLocation.title forKey:@"address"];
    }
    return ret;
}

+ (NSDictionary *)_convertRegion:(BMKCoordinateRegion)region
{
    NSMutableDictionary* ret = [NSMutableDictionary dictionary];
    [ret setObject:@(region.center.latitude) forKey:@"latitude"];
    [ret setObject:@(region.center.longitude) forKey:@"longitude"];
    [ret setObject:@(region.span.latitudeDelta) forKey:@"latitudeDelta"];
    [ret setObject:@(region.span.longitudeDelta) forKey:@"longitudeDelta"];
    return ret;
}

@end
