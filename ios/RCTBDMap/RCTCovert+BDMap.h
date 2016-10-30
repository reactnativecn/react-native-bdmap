//
//  RCTCovert+BDMap.h
//  RCTBDMap
//
//  Created by DengYun on 4/27/16.
//  Copyright © 2016 DengYun. All rights reserved.
//

#import "RCTConvert.h"

#import <BaiduMapAPI_Base/BMKBaseComponent.h>//引入base相关所有的头文件
#import <BaiduMapAPI_Utils/BMKUtilsComponent.h>//引入计算工具所有的头文件
#import <BaiduMapAPI_Map/BMKMapComponent.h>//引入地图功能所有的头文件

@interface RCTConvert(BaiduMap)

+ (BMKMapPoint)BMKMapPoint:(id)json;
+ (CLLocationCoordinate2D)CLLocationCoordinate2D:(id)json;
+ (BMKCoordinateRegion)BMKCoordinateRegion:(id)json;
+ (BMKCoordinateSpan)BMKCoordinateSpan:(id)json;

@end
