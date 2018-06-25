import { NativeModules, NativeEventEmitter } from 'react-native'

const SystemSettingNative = NativeModules.SystemSetting
const eventEmitter = new NativeEventEmitter(SystemSettingNative)

export default class SystemSetting {
    static async getVolume(type = 'music') {
        return await SystemSettingNative.getVolume(type)
    }

    static setVolume(val, config = {}) {
        if (typeof (config) === 'string') {
            console.log('setVolume(val, type) is deprecated since 1.2.2, use setVolume(val, config) instead')
            config = { type: config }
        }
        config = Object.assign({
            playSound: false,
            type: 'music',
            showUI: false
        }, config)
        SystemSettingNative.setVolume(val, config)
    }

    static addVolumeListener(callback) {
        return eventEmitter.addListener('EventVolume', callback)
    }

    static removeVolumeListener(listener) {
        listener && listener.remove()
    }
}