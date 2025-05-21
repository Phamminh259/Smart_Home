export interface ACValueDigest {
    deviceId: number,
    currentTemp: number,
    targetTemp: number,
    unit: string,
    mode: string,
    fanSpeed: number,
    health: boolean,
    fungusPrevent: boolean
}