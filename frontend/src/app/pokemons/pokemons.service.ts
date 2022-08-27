import {Injectable} from '@angular/core';
import {Pokemon} from './pokemon'
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";

export type Operator = "gt" | "lt" | "eq"

export interface SearchParams {
  name?: string
  weight?: number
  weightOperator?: Operator
  height?: number
  heightOperator?: Operator
  page?: number
}

export interface SearchResponse {
  start: number
  limit: number
  total: number
  records: Pokemon[]
}

@Injectable({
  providedIn: 'root'
})
export class PokemonsService {

  constructor(
    private http: HttpClient
  ) {
  }

  search(params: Partial<SearchParams>) {
    return this.http.get<SearchResponse>(`/api/v1/pokemons`, {
      responseType: "json",
      observe: "body",
      params: params as Record<string, string | number>

    },)
  }
}
