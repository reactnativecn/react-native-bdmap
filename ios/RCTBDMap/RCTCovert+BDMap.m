//
//  RCTCovert+BDMap.m
//  RCTBDMap
//
//  Created by DengYun on 4/27/16.
//  Copyright © 2016 DengYun. All rights reserved.
//

#import "RCTCovert+BDMap.h"

@implementation RCTConvert(BaiduMap)

+ (BMKMapPoint)BMKMapPoint:(id)json{
    return BMKMapPointForCoordinate([self CLLocationCoordinate2D:json]);
}

+ (CLLocationCoordinate2D)CLLocationCoordinate2D:(id)json {
    json = [self NSDictionary:json];
    
    double latitude = [[json valueForKey:@"latitude"] doubleValue];
    double longitude =[[json valueForKey:@"longitude"] doubleValue];
    return CLLocationCoordinate2DMake(latitude, longitude);
}

+ (BMKCoordinateSpan)BMKCoordinateSpan:(id)json {
    json = [self NSDictionary:json];
    
    double latitudeDelta = [[json valueForKey:@"latitudeDelta"] doubleValue];
    double longitudeDelta =[[json valueForKey:@"longitudeDelta"] doubleValue];
    return BMKCoordinateSpanMake(latitudeDelta, longitudeDelta);
}

+ (BMKCoordinateRegion)BMKCoordinateRegion:(id)json{
    json = [self NSDictionary:json];
    return BMKCoordinateRegionMake([RCTConvert CLLocationCoordinate2D:json], [RCTConvert BMKCoordinateSpan:json]);
}

@end
