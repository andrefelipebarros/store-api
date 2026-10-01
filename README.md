# Store API

[![CI/CD](https://github.com/andrefelipebarros/store-api/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/andrefelipebarros/store-api/actions/workflows/ci-cd.yml)

API REST de uma loja virtual: cadastro e login com **JWT**, catálogo de produtos com paginação e busca, e pedidos com controle de estoque.

![Java](https://img.shields.io/badge/Java-21-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-green) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue) ![Docker](https://img.shields.io/badge/Docker-ready-2496ED)

## Por que criei este projeto

Estou me preparando para começar minha carreira como desenvolvedor Java backend júnior. Depois de
estudar os conceitos separados (JPA, Spring Security, transações, Docker), quis juntar tudo em um
sistema que se comportasse como um backend de verdade, e não como exercícios soltos.

Escolhi uma loja virtual porque ela tem um problema que me interessava: **o que acontece com o
estoque quando duas pessoas compram o mesmo produto ao mesmo tempo?** Esse tipo de pergunta só
aparece quando você constrói algo que tem regras de negócio, e foi a partir dela que o projeto
ganhou forma: catálogo público, área autenticada, perfil de administrador e pedidos que mexem
no estoque de forma consistente.

Cada decisão técnica abaixo foi uma dúvida que precisei resolver, e o projeto continua evoluindo
conforme eu aprendo.

## O que eu queria aprender

- Autenticação stateless com JWT e controle de acesso por perfil (público, usuário e ADMIN)
- Garantir consistência entre pedido e estoque usando transações
- Lidar com concorrência em atualizações de estoque
- Versionar o banco com Flyway em vez de deixar o Hibernate criar as tabelas
- Entregar o projeto como um produto: testes, Docker e pipeline de CI/CD

## Tecnologias

- Java 21, Spring Boot 3 (Web, Data JPA, Security, Validation, Actuator)
- PostgreSQL 16 com migrations via **Flyway**
- Autenticação stateless com **JWT** (jjwt) e senhas com BCrypt
- Documentação **OpenAPI/Swagger**
- Testes com JUnit 5, Mockito e AssertJ
- Docker, Docker Compose e **GitHub Actions** (CI)

## Como rodar

Com Docker (sobe banco e API):

```bash
docker compose up --build
```

Sem Docker (precisa de um PostgreSQL local com banco `store`, usuário `store`, senha `store`):

```bash
mvn spring-boot:run
```

Swagger: http://localhost:8080/swagger-ui.html

Um usuário administrador é criado na primeira execução: `admin@store.com` / `admin123` (configure com as variáveis `ADMIN_EMAIL` e `ADMIN_PASSWORD`).

## Endpoints

| Método | Rota | Acesso |
|--------|------|--------|
| POST | `/api/auth/register` | Público |
| POST | `/api/auth/login` | Público |
| GET | `/api/products?name=&page=&size=` | Público |
| GET | `/api/products/{id}` | Público |
| POST / PUT / DELETE | `/api/products` | ADMIN |
| POST | `/api/orders` | Autenticado |
| GET | `/api/orders` | Autenticado (só os seus pedidos) |
| PATCH | `/api/orders/{id}/cancel` | Autenticado (devolve o estoque) |

Exemplo rápido:

```bash
curl -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" \
  -d '{"email":"admin@store.com","password":"admin123"}'
```

Use o token retornado no header `Authorization: Bearer <token>`.

## Decisões técnicas

- **Arquitetura por feature** (`auth`, `product`, `order`), cada pacote com controller, service, repository e DTOs. Assim, tudo sobre um assunto fica junto e é fácil de achar.
- **DTOs com records** para nunca expor entidades JPA na API.
- **Erros padronizados** com `ProblemDetail` (RFC 7807) num `@RestControllerAdvice`.
- **Lock otimista** (`@Version`) em `Product` para evitar venda duplicada em pedidos concorrentes. Escolhi essa abordagem porque conflitos de estoque são raros e assim não bloqueio linhas do banco a cada leitura.
- **Transações** na criação e no cancelamento de pedidos: estoque e pedido são gravados juntos ou nada é gravado.
- **Preço congelado no item do pedido** (`unit_price`): se o preço do produto mudar depois, pedidos antigos continuam com o valor da época da compra.
- **Constraints no banco** (`CHECK` em preço, estoque e quantidade): o banco também protege os dados, não só o código Java.
- `open-in-view` desligado e `ddl-auto: validate`: o schema é responsabilidade do Flyway.

## Desafios

- **Estoque e pedido andando juntos:** um pedido com vários itens não pode abater estoque de um produto e falhar no seguinte. A solução foi concentrar a criação do pedido em um único método transacional, que desfaz tudo se algum item não tiver estoque.
- **Concorrência:** entender como o `@Version` faz a segunda de duas compras simultâneas falhar em vez de vender além do estoque.
- **Cancelamento seguro:** devolver o estoque apenas de pedidos pendentes, e só para quem é dono do pedido (a busca já filtra pelo e-mail do usuário autenticado).
- **JWT stateless:** configurar o filtro de autenticação, a política de sessão e as regras por rota e por perfil no Spring Security.
- **Pipeline:** fazer o CI compilar, testar e publicar a imagem no GitHub Container Registry, com o Dependabot mantendo as dependências atualizadas.

**O que faria diferente hoje:** trataria explicitamente o conflito de lock otimista, devolvendo `409 Conflict` com uma mensagem clara em vez de deixar o erro genérico, e adicionaria testes de integração com Testcontainers para validar a concorrência contra um PostgreSQL real.

## Testes

```bash
mvn test
```

## CI/CD

O workflow `.github/workflows/ci-cd.yml` roda no GitHub Actions:

- **Pull requests:** compila e executa os testes (`mvn verify`).
- **Push na `main` ou tag `v*`:** além dos testes, testa e empacota a aplicação e publica a imagem docker no GitHub Container Registry (`ghcr.io/andrefelipebarros/store-api`).
- O Dependabot (`.github/dependabot.yml`) abre PRs semanais para atualizar dependências, Dockerfile e actions.

## Roadmap

- [x] Cadastro e login com JWT, com perfis USER e ADMIN
- [x] Catálogo com paginação e busca por nome
- [x] Pedidos com controle de estoque e cancelamento
- [x] CI/CD com GitHub Actions e imagem no GHCR
- [ ] Testes de integração com Testcontainers
- [ ] Tratamento de conflito de lock otimista (409)
- [ ] Refresh token
- [ ] Pagamento simulado (status PAID)
- [ ] Cache de catálogo com Redis
