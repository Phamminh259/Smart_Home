export interface ChartData {
    name: string,
    value: number
    timestamp: Date,
    tags: Map<string,string>
}