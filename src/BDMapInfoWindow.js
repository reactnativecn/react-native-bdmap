/**
 * Created by tdzl2003 on 4/25/16.
 */

import React, {
  requireNativeComponent,
  PropTypes,
  View,
  UIManager,
} from 'react-native'

var resolveAssetSource = require('react-native/Libraries/Image/resolveAssetSource');

const defaultMarkerIcon = require('../assets/icon.png');

class BDMapInfoWindow extends React.Component {
  static propTypes = {
    /** 继承自View的属性 **/
    ...View.propTypes,

    /** 属性 **/

    // 标注的位置
    latitude: PropTypes.number,
    longitude: PropTypes.number,

    yOffset: PropTypes.number,
  };
  static defaultProps = {
  };
  constructor(props) {
    super(props);
    this._location = {
      latitude: props.latitude,
      longitude: props.longitude,
    };
  }

  componentWillReceiveProps(nextProps){
    if (nextProps.latitude !== this._location.latitude || nextProps.longitude !== this._location.longitude){
      this._location = {
        latitude: nextProps.latitude,
        longitude: nextProps.longitude,
      };
    }
  }
  render() {
    const {yOffset, children, ...others} = this.props;
    return <RCTBDMapInfoWindow
        location={this._location}
        yOffset={yOffset}
       >
        <View {...others} renderToHardwareTextureAndroid={true}>
          {children}
        </View>
      </RCTBDMapInfoWindow>;
  }
}

var RCTBDMapInfoWindow = requireNativeComponent(`RCTBDMapInfoWindow`, BDMapInfoWindow, {
  nativeOnly: {
    location: true,
  }
});

module.exports = BDMapInfoWindow;
