namespace BookShelf.Application.Results;

public class Error : IError
{
    public Error(string message)
    {
        Message = message;
    }

    public string Message { get; }

    public override string ToString() => Message;
}
