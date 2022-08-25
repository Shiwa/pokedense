import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'Pokedense';

  mockRequest = {
    page: 1,
    name: undefined,
    weight: undefined,
    weightOperator: undefined,
    height: undefined,
    heightOperator: undefined,
  }

  mockResponse = {"start":0,"limit":20,"total":1154,"records":[{"name":"bulbasaur","weight":69.0,"weightUnit":"hg","height":7.0,"heightUnit":"dm"},{"name":"ivysaur","weight":130.0,"weightUnit":"hg","height":10.0,"heightUnit":"dm"},{"name":"venusaur","weight":1000.0,"weightUnit":"hg","height":20.0,"heightUnit":"dm"},{"name":"charmander","weight":85.0,"weightUnit":"hg","height":6.0,"heightUnit":"dm"},{"name":"charmeleon","weight":190.0,"weightUnit":"hg","height":11.0,"heightUnit":"dm"},{"name":"charizard","weight":905.0,"weightUnit":"hg","height":17.0,"heightUnit":"dm"},{"name":"squirtle","weight":90.0,"weightUnit":"hg","height":5.0,"heightUnit":"dm"},{"name":"wartortle","weight":225.0,"weightUnit":"hg","height":10.0,"heightUnit":"dm"},{"name":"blastoise","weight":855.0,"weightUnit":"hg","height":16.0,"heightUnit":"dm"},{"name":"caterpie","weight":29.0,"weightUnit":"hg","height":3.0,"heightUnit":"dm"},{"name":"metapod","weight":99.0,"weightUnit":"hg","height":7.0,"heightUnit":"dm"},{"name":"butterfree","weight":320.0,"weightUnit":"hg","height":11.0,"heightUnit":"dm"},{"name":"weedle","weight":32.0,"weightUnit":"hg","height":3.0,"heightUnit":"dm"},{"name":"kakuna","weight":100.0,"weightUnit":"hg","height":6.0,"heightUnit":"dm"},{"name":"beedrill","weight":295.0,"weightUnit":"hg","height":10.0,"heightUnit":"dm"},{"name":"pidgey","weight":18.0,"weightUnit":"hg","height":3.0,"heightUnit":"dm"},{"name":"pidgeotto","weight":300.0,"weightUnit":"hg","height":11.0,"heightUnit":"dm"},{"name":"pidgeot","weight":395.0,"weightUnit":"hg","height":15.0,"heightUnit":"dm"},{"name":"rattata","weight":35.0,"weightUnit":"hg","height":3.0,"heightUnit":"dm"},{"name":"raticate","weight":185.0,"weightUnit":"hg","height":7.0,"heightUnit":"dm"}]}
  pokemons = this.mockResponse.records

  // TODO add logic to search pokemons

  hasPreviousPage() {
    return true // this.mockRequest.page > 1
  }

  goToPreviousPage() {
    this.goToPage(this.mockRequest.page - 1)
  }

  hasNextPage() {
    return this.mockResponse.start + this.mockResponse.limit < this.mockResponse.total
  }

  goToNextPage() {
    this.goToPage(this.mockRequest.page + 1)
  }

  goToPage(page: Number) {
    // TODO
    console.log(`goToPage(${page})`)
  }
}
