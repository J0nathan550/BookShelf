import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { StatisticsDto } from '../models/statistics.model';

@Injectable({ providedIn: 'root' })
export class StatisticsService {
  constructor(private http: HttpClient) {}

  getStatistics(): Observable<StatisticsDto> {
    return this.http.get<StatisticsDto>(`${environment.apiUrl}/statistics`);
  }
}
