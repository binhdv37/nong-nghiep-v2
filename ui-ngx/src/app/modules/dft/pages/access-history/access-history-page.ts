import {SortOrder} from "@shared/models/page/sort-order";
import {PageLink} from "@shared/models/page/page-link";

export class AccessHistoryPage extends PageLink {

  startTime: number;
  endTime: number;
  entityType: string;

  constructor(pageSize: number, page: number = 0, textSearch: string = null, sortOrder: SortOrder = null,
              startTime: number = null, endTime: number = null, entityType: string = null) {
    super(pageSize, page, textSearch, sortOrder);
    this.startTime = startTime;
    this.endTime = endTime;
    this.entityType = entityType;
  }

  public nextPageLink(): AccessHistoryPage {
    return new AccessHistoryPage(this.pageSize, this.page + 1, this.textSearch, this.sortOrder, this.startTime, this.endTime, this.entityType);
  }

  public toQuery(): string {
    let query = super.toQuery();
    if (!!this.startTime) {
      query += `&startTime=${this.startTime}`;
    }
    if (!!this.endTime) {
      query += `&endTime=${this.endTime}`;
    }
    if (!!this.entityType) {
      query += `&entityType=${this.entityType}`;
    }
    return query;
  }
}
