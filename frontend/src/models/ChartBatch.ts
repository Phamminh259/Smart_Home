import {ChartData} from "./ChartData.ts";

export interface ChartBatch {
    batch: ChartData[]
    hasMore: boolean
}