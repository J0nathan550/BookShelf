FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build
WORKDIR /src

COPY BookShelf.Domain/BookShelf.Domain.csproj BookShelf.Domain/
COPY BookShelf.Application/BookShelf.Application.csproj BookShelf.Application/
COPY BookShelf.Infrastructure/BookShelf.Infrastructure.csproj BookShelf.Infrastructure/
COPY BookShelf.Server/BookShelf.Server.csproj BookShelf.Server/

RUN dotnet restore BookShelf.Server/BookShelf.Server.csproj

COPY . .

RUN dotnet publish BookShelf.Server/BookShelf.Server.csproj \
    -c Release \
    -o /app/publish \
    --no-restore

FROM mcr.microsoft.com/dotnet/aspnet:9.0 AS runtime
WORKDIR /app

RUN mkdir -p wwwroot/covers

COPY --from=build /app/publish .

ENV ASPNETCORE_URLS=http://+:5000
EXPOSE 5000

ENTRYPOINT ["dotnet", "BookShelf.Server.dll"]
