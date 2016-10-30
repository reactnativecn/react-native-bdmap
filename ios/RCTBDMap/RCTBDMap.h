//
//  RCTAlipay.h
//  RCTAlipay
//
//  Created by DengYun on 4/21/16.
//  Copyright © 2016 DengYun. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "RCTBridgeModule.h"
#import <BaiduMapAPI_Location/BMKLocationComponent.h>//引入定位功能所有的头文件

@interface RCTBDMap : NSObject <RCTBridgeModule, BMKLocationServiceDelegate>

+ (NSDictionary *)_convertLocation:(BMKUserLocation *)userLocation;
+ (NSDictionary *)_convertRegion:(BMKCoordinateRegion)region;

@end
