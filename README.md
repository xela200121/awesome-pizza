# Awesome Pizza

## Original assignment

Come pizzeria "Awesome Pizza" voglio creare il mio nuovo portale per gestire gli ordini dei miei clienti. 
Il portale non richiede la registrazione dell'utente per poter ordinare le sue pizze.

Il pizzaiolo vede la coda degli ordini e li può prendere in carico uno alla volta. 
Quando la pizza è pronta, il pizzaiolo passa all'ordine successivo. 
L'utente riceve il suo codice d'ordine e può seguire lo stato dell'ordine fino all'evasione.

Come team, procediamo allo sviluppo per iterazioni. 
Decidiamo che nella prima iterazione non sarà disponibile un'interfaccia grafica, 
ma verranno create delle API al fine di ordinare le pizze e aggiornarne lo stato.

Decidiamo di utilizzare il framework Spring e Java (versione 17 o superiore). 
Decidiamo di progettare anche i test di unità sul codice oggetto di sviluppo.

## Solution overview

This first iteration exposes REST APIs only, with no UI.

Customers can create orders without registration and track them through a public order code.
The pizzaiolo can take one order at a time and complete it.

API details are available in Swagger.

## Tech stack

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- H2
- Swagger/OpenAPI

## Run

```powershell
.\mvnw.cmd spring-boot:run
```

The application starts on `http://localhost:8080`.

## Test

```powershell
.\mvnw.cmd test
```

## Swagger

OpenAPI documentation is available at:

`http://localhost:8080/swagger-ui.html`

## Design notes

- The database is H2 in-memory.
- DTOs are separated from JPA entities.
- Business rules are handled in the service layer.
- Only one order can be `IN_PROGRESS` at a time.
- Order codes use the `AW-` prefix.