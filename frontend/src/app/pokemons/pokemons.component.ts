import { Component, OnInit } from '@angular/core';
import { PokemonsService, SearchParams, SearchResponse } from "./pokemons.service";
import { Observable } from "rxjs";
import { FormBuilder } from '@angular/forms';


@Component({
  selector: 'app-pokemons',
  templateUrl: './pokemons.component.html',
  styleUrls: ['./pokemons.component.css']
})
export class PokemonsComponent implements OnInit {

  constructor(
    private pokemonsService: PokemonsService,
    private formBuilder: FormBuilder,
  ) {
  }

  searchForm = this.formBuilder.group<SearchParams>({
    name: '',
    height: 0,
    heightOperator: "gt",
    weight: 0,
    weightOperator: "gt",
  })

  private page = 1
  private currentParams?: Partial<SearchParams>
  private query!: Observable<SearchResponse>
  lastResponse?: SearchResponse
  loading: boolean = false

  ngOnInit(): void {
    this.search(1)
  }

  preparedFormValue(): Partial<SearchParams> {
    return Object.fromEntries(Object.entries(this.searchForm.value)
      .filter((k, v) => v !== null && v !== undefined)
    )
  }


  search(page: number) {
    const params: Partial<SearchParams> = {
      ...this.preparedFormValue(),
      page
    }
    if (JSON.stringify(params) != JSON.stringify(this.currentParams)) {
      this.currentParams = params
      this.loading = true
      this.query = this.pokemonsService.search(params)
      this.query.subscribe(response => {
        this.loading = false
        this.lastResponse = response
        this.page = page
      })
    }
    return this.query
  }

  hasPreviousPage() {
    return (this.page ?? 0) > 1
  }

  goToPreviousPage() {
    if (this.page)
      this.goToPage(this.page - 1)
  }

  hasNextPage() {
    if (!this.lastResponse)
      return false
    const { total, limit, start } = this.lastResponse;
    return start + limit < total
  }

  goToNextPage() {
    if (this.page)
      this.goToPage(this.page + 1)
  }

  goToPage(page: number) {
    this.search(page)
  }

}
