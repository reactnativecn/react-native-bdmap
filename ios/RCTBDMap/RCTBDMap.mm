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
#import <BaiduMapAPI_Search/BMKSearchComponent.h>//引入搜索所需的头文件

static BMKMapManager* _mapManager;

@interface ReverseGeoCodeResult : NSObject<BMKGeoCodeSearchDelegate>
- initWithResolve: (RCTPromiseResolveBlock) resolve reject: (RCTPromiseRejectBlock) reject;
@end

static NSMutableArray* arr = [[NSMutableArray alloc] init];

@implementation ReverseGeoCodeResult
{
    RCTPromiseResolveBlock _resolve;
    RCTPromiseRejectBlock _reject;
}

- (instancetype)initWithResolve: (RCTPromiseResolveBlock) resolve reject: (RCTPromiseRejectBlock) reject{
    if ([self init]) {
        self->_resolve = resolve;
        self->_reject = reject;
        return self;
    }
    return nil;
}

+ (instancetype)resultWithResolve: (RCTPromiseResolveBlock) resolve reject: (RCTPromiseRejectBlock) reject{
    ReverseGeoCodeResult* ret = [[ReverseGeoCodeResult alloc] initWithResolve:resolve reject:reject];
    [arr addObject:ret];
    return ret;
}

- (void)onGetReverseGeoCodeResult:(BMKGeoCodeSearch *)searcher result:(BMKReverseGeoCodeResult *)result errorCode:(BMKSearchErrorCode)error {
    if (error != BMK_SEARCH_NO_ERROR){
        self->_reject([NSString stringWithFormat:@"%d", (int)error], @"Search failed", nil);
        [arr removeObject:self];
        return;
    }
    self->_resolve(@{
                     @"formattedAddress" : result.address,
                     @"business": result.businessCircle,
                     @"pois": [RCTBDMap _convertPoiList: result.poiList],
                     @"addressComponent": [RCTBDMap _convertAddress: result.addressDetail],
                     });
    [arr removeObject:self];
}

@end


@interface PoiSearchResult : NSObject<BMKPoiSearchDelegate>
- initWithResolve: (RCTPromiseResolveBlock) resolve reject: (RCTPromiseRejectBlock) reject;
@end

@implementation PoiSearchResult
{
    RCTPromiseResolveBlock _resolve;
    RCTPromiseRejectBlock _reject;
}

- (instancetype)initWithResolve: (RCTPromiseResolveBlock) resolve reject: (RCTPromiseRejectBlock) reject{
    if ([self init]) {
        self->_resolve = resolve;
        self->_reject = reject;
        return self;
    }
    return nil;
}

- (void)onGetPoiResult:(BMKPoiSearch*)searcher result:(BMKPoiResult*)result errorCode:(BMKSearchErrorCode)error
{
    if (error != BMK_SEARCH_NO_ERROR){
        self->_reject([NSString stringWithFormat:@"%d", (int)error], @"Search failed", nil);
        return;
    }
    self->_resolve(@{
                     @"totalPages": @(result.pageNum),
                     @"totalCount": @(result.totalPoiNum),
                     @"pois": [RCTBDMap _convertPoiList: result.poiInfoList],
                     });
}

@end

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

RCT_EXPORT_METHOD(geoReverse: (CLLocationCoordinate2D) pt resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)
{
    BMKGeoCodeSearch* _searcher =[[BMKGeoCodeSearch alloc]init];
    _searcher.delegate = [ReverseGeoCodeResult resultWithResolve:resolve reject:reject];
    
    BMKReverseGeoCodeOption * options = [[BMKReverseGeoCodeOption alloc]init];
    options.reverseGeoPoint = pt;
    BOOL flag = [_searcher reverseGeoCode:options];
    if (!flag){
        reject(@"invokeFailed", @"reverseGeoCode return false", nil);
        return;
    }
}

RCT_EXPORT_METHOD(poiSearch:(CLLocationCoordinate2D) pt option: (NSDictionary*) opt resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)
{
    BMKPoiSearch* _searcher = [[BMKPoiSearch alloc] init];
    _searcher.delegate = [[PoiSearchResult alloc] initWithResolve: resolve reject: reject];
    
    BMKNearbySearchOption * options = [[BMKNearbySearchOption alloc] init];
    options.location =pt;
    if ( [opt valueForKey: @"keyword"] != nil){
        options.keyword = [opt valueForKey : @"keyword"];
    }
    if ( [opt valueForKey: @"sortMode"] != nil) {
        if ([@"nearToFar" isEqualToString:[opt valueForKey: @"sortMode"]]){
            options.sortType = BMK_POI_SORT_BY_DISTANCE;
        } else {
            options.sortType = BMK_POI_SORT_BY_COMPOSITE;
        }
    }
    if ( [opt valueForKey: @"radius"] != nil) {
        options.radius = [[opt valueForKey:@"radius"] intValue];
    }
    if ( [opt valueForKey: @"pageIndex"] != nil) {
        options.pageIndex = [[opt valueForKey:@"pageIndex"] intValue];
    }
    if ( [opt valueForKey: @"pageCapacity"] != nil) {
        options.pageCapacity = [[opt valueForKey:@"pageCapacity"] intValue];
    }
    options.location = pt;
    BOOL flag = [_searcher poiSearchNearBy:options];
    if (!flag){
        reject(@"invokeFailed", @"reverseGeoCode return false", nil);
        return;
    }
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

+ (NSDictionary *)_convertCLLocation:(CLLocationCoordinate2D)location
{
    NSMutableDictionary* ret = [NSMutableDictionary dictionary];
    [ret setObject:@(location.latitude) forKey:@"latitude" ];
    [ret setObject:@(location.longitude) forKey:@"longitude" ];
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

+ (NSArray *) _convertPoiList: (NSArray*) list
{
    NSMutableArray* ret = [NSMutableArray arrayWithCapacity:list.count];
    for (size_t i = 0; i<list.count; ++i) {
        ret[i] = [RCTBDMap _convertPoi:list[i]];
    }
    return ret;
}

+ (NSDictionary *)_convertPoi: (BMKPoiInfo*) info
{
    NSMutableDictionary* dict = [NSMutableDictionary dictionary];
    dict[@"name"] = info.name;
    dict[@"uid"] = info.uid;
    dict[@"address"] = info.address;
    dict[@"city"] = info.city;
    dict[@"phoneNum"] = info.phone;
    dict[@"postCode"] = info.postcode;
    dict[@"type"] = @(info.epoitype);
    dict[@"location"] = [RCTBDMap _convertCLLocation:info.pt];
    return dict;
}

+ (NSDictionary *)_convertAddress: (BMKAddressComponent*) a
{
    NSMutableDictionary* dict = [NSMutableDictionary dictionary];
    dict[@"city"] = a.city;
    dict[@"streetNumber"] = a.streetNumber;
    dict[@"street"] = a.streetName;
    dict[@"district"] = a.district;
    dict[@"province"] = a.province;
    return dict;
}

@end
