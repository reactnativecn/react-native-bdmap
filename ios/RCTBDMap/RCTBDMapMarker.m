//
//  RCBBDMapMarker.m
//  RCTBDMap
//
//  Created by DengYun on 4/28/16.
//  Copyright © 2016 DengYun. All rights reserved.
//

#import "RCTBDMapMarker.h"
#import <React/RCTUIManager.h>

@implementation RCTBDMapMarker

@synthesize key = _key;
@synthesize location = _location;
@synthesize annoView = _annoView;
@synthesize image = _image;
@synthesize imageUrl = _imageUrl;

- (instancetype)init {
    return self;
}

- (CLLocationCoordinate2D)coordinate
{
    return _location;
}

- (void)setCoordinate:(CLLocationCoordinate2D)newCoordinate
{
    _location = newCoordinate;
}

- (void)setImage:(UIImage *)image {
    self->_image = image;
    if (self->_annoView) {
        self->_annoView.image = image;
    }
}

- (NSString *)title {
    return @"";
}

@end
