namespace BookShelf.Application.Results;

public class Result<T>
{
    public bool IsSuccess { get; }
    public T? Value { get; }

    public IReadOnlyList<IError> Errors { get; }

    private Result(T value)
    {
        IsSuccess = true;
        Value = value;
        Errors = [];
    }

    private Result(params IError[] errors)
    {
        IsSuccess = false;
        Value = default;
        Errors = errors.AsReadOnly();
    }
    
    public static Result<T> Ok(T value) => new(value);

    public static Result<T> Fail(string message) => new(new Error(message));

    public static Result<T> Fail(params IError[] errors) => new(errors);

    public static Result<T> Fail(params string[] messages)
        => new(messages.Select(m => (IError)new Error(m)).ToArray());
}

public class Result
{
    public bool IsSuccess { get; }

    public IReadOnlyList<IError> Errors { get; }

    private Result()
    {
        IsSuccess = true;
        Errors = [];
    }

    private Result(params IError[] errors)
    {
        IsSuccess = false;
        Errors = errors.AsReadOnly();
    }

    public static Result Ok() => new();

    public static Result Fail(string message) => new(new Error(message));

    public static Result Fail(params IError[] errors) => new(errors);

    public static Result Fail(params string[] messages)
        => new(messages.Select(m => (IError)new Error(m)).ToArray());
}