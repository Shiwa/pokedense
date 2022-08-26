import {Component, OnInit} from '@angular/core';
import {PokemonsService, SearchParams, SearchResponse} from "./pokemons.service";
import {Observable} from "rxjs";

@Component({
  selector: 'app-pokemons',
  templateUrl: './pokemons.component.html',
  styleUrls: ['./pokemons.component.css']
})
export class PokemonsComponent implements OnInit {

  constructor(
    private pokemonsService: PokemonsService
  ) {
  }

  private params: SearchParams = {page: 1}
  private currentParams?: SearchParams
  private query!: Observable<SearchResponse>
  private lastResponse?: SearchResponse

  ngOnInit(): void {
    this.query = this.pokemonsService.search(this.params)
  }

  searchWithParams(params: SearchParams) {
    this.params = params
    return this.search()
  }


  search() {
    if (this.params !== this.currentParams) {
      this.currentParams = this.params
      this.query = this.pokemonsService.search(this.params)
      this.query.subscribe(response => this.lastResponse = response)
    }
    return this.query
  }

  hasPreviousPage() {
    return this.params.page ?? 0 > 1
  }

  goToPreviousPage() {
    if (this.params.page)
      this.goToPage(this.params.page - 1)
  }

  hasNextPage() {
    if (!this.lastResponse)
      return false
    const {total, limit, start} = this.lastResponse;
    return start + limit < total
  }

  goToNextPage() {
    if (this.params.page)
      this.goToPage(this.params.page + 1)
  }

  goToPage(page: number) {
    this.params = {...this.params, page}
    this.pokemonsService.search(this.params)
  }

}
