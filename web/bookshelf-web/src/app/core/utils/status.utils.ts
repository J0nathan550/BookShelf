export function statusKey(status: string | undefined | null): string {
  switch (status) {
    case 'Want to Read':      return 'STATUS.WANT_TO_READ';
    case 'Currently Reading': return 'STATUS.CURRENTLY_READING';
    case 'Finished':          return 'STATUS.FINISHED';
    case 'Lent Out':          return 'STATUS.LENT_OUT';
    default:                  return 'STATUS.AVAILABLE';
  }
}
