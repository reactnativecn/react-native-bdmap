//
//  RCTAlipay.h
//  RCTAlipay
//
//  Created by DengYun on 4/21/16.
//  Copyright © 2016 DengYun. All rights reserved.
//

#import <Foundation/Foundation.h>

#import <React/RCTBridgeModule.h>
#import <BaiduMapAPI_Location/BMKLocationComponent.h>//引入定位功能所有的头文件
#import <BaiduMapAPI_Search/BMKSearchComponent.h>//引入搜索所需的头文件

@interface RCTBDMap : NSObject <RCTBridgeModule, BMKLocationServiceDelegate>

+ (NSDictionary *)_convertLocation:(BMKUserLocation *)userLocation;
+ (NSDictionary *)_convertRegion:(BMKCoordinateRegion)region;
+ (NSArray *) _convertPoiList: (NSArray*) list;
+ (NSDictionary *)_convertAddress: (BMKAddressComponent*) address;

@end
