export interface StatisticsDto {
  totalBooks: number;
  wantToRead: number;
  currentlyReading: number;
  finished: number;
  booksReadThisYear: number;
  genreDistribution: { [genre: string]: number };
}
