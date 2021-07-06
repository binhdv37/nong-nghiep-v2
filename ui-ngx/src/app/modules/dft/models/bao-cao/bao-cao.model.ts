export interface BcSingleData {
  name: string;
  value: number;
}

export interface BcMultiData {
  name: string;
  series: SeriesData[];
}

export interface SeriesData{
  name: string;
  value: number;
}
