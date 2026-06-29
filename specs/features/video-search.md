# Pesquisa de Vídeos

## Contexto
Usuários com assinatura ativa podem pesquisar vídeos usando diversos critérios.
O sistema oferece busca por palavras-chave, filtros por metadados e ordenação por data.
Resultados devem ser paginados para melhor performance.

---

## Cenário: Busca por palavra-chave

**Given** que o usuário está autenticado
**And** que possui uma assinatura ativa
**And** que existem vídeos no catálogo
**When** digita "programação" no campo de busca
**Then** o sistema retorna todos os vídeos que contêm "programação" no título ou descrição
**And** exibe 20 resultados por página (paginação)
**And** mostra o total de resultados encontrados
**And** ordena por relevância (padrão)

---

## Cenário: Busca por título exato

**Given** que o usuário está autenticado com assinatura ativa
**When** busca por "Como Aprender Python"
**Then** exibe vídeos com títulos que correspondem à busca
**And** oferece opção de filtrar por título exato (exact match)
**And** diferencia maiúsculas/minúsculas conforme parâmetro

---

## Cenário: Filtro por categoria

**Given** que o usuário está em busca de vídeos
**And** que existem categorias cadastradas (ex: Programação, Design, Negócios)
**When** clica no filtro "Categoria"
**Then** pode selecionar uma ou múltiplas categorias
**And** o sistema refina os resultados para apenas vídeos daquela(s) categoria(s)

---

## Cenário: Filtro por criador/canal

**Given** que o usuário deseja ver vídeos de um criador específico
**When** aplica filtro por criador (ex: "Canal do João")
**Then** exibe apenas vídeos publicados por aquele criador
**And** mostra informações do criador (nome, descrição, número de inscritos)

---

## Cenário: Ordenação por data (mais recentes)

**Given** que o usuário tem resultados de busca
**When** seleciona ordenação "Mais Recentes"
**Then** os vídeos são ordenados por data de publicação (descendente)
**And** vídeos publicados hoje aparecem primeiro

---

## Cenário: Ordenação por data (mais antigos)

**Given** que o usuário tem resultados de busca
**When** seleciona ordenação "Mais Antigos"
**Then** os vídeos são ordenados por data de publicação (ascendente)
**And** vídeos mais antigos aparecem primeiro

---

## Cenário: Ordenação por relevância

**Given** que o usuário faz uma busca por keyword
**When** não especifica ordenação
**Then** utiliza ordenação por relevância (padrão)
**And** vídeos com mais visualizações/relevância aparecem primeiro

---

## Cenário: Ordenação por duração

**Given** que o usuário está visualizando resultados
**When** seleciona ordenação "Duração"
**Then** pode escolher "Mais curtos" ou "Mais longos"
**And** os vídeos são ordenados pela duração do arquivo

---

## Cenário: Filtro por duração do vídeo

**Given** que o usuário quer vídeos de até 10 minutos
**When** aplica filtro "Duração: até 10 minutos"
**Then** o sistema retorna apenas vídeos com duração ≤ 10 minutos

---

## Cenário: Filtro por data de publicação

**Given** que o usuário quer vídeos dos últimos 7 dias
**When** aplica filtro "Esta semana"
**Then** o sistema retorna apenas vídeos publicados nos últimos 7 dias
**And** oferece outras opções: "Este mês", "Este ano", "Todos"

---

## Cenário: Filtro por resolução

**Given** que o usuário quer vídeos em alta qualidade
**When** aplica filtro "Mínimo 1080p"
**Then** o sistema retorna apenas vídeos que foram processados em 1080p
**And** exclui vídeos com apenas 480p ou 720p

---

## Cenário: Combinação de múltiplos filtros

**Given** que o usuário quer combinar múltiplos critérios
**When** aplica filtros: Categoria="Programação" + Criador="João" + Duração="Até 15min" + Ordem="Mais Recentes"
**Then** o sistema refina os resultados considerando TODOS os filtros
**And** exibe apenas vídeos que correspondem a TODOS os critérios

---

## Cenário: Busca com paginação

**Given** que a busca retornou 150 resultados
**When** exibe a primeira página com 20 resultados
**Then** mostra botões de navegação "Próxima" e "Anterior"
**And** permite saltar diretamente para uma página específica
**And** mostra indicador "Página X de Y"

---

## Cenário: Busca vazia retorna lista padrão

**Given** que o usuário acessa a seção de vídeos sem fazer buscas
**When** nenhum termo de busca é inserido
**Then** o sistema exibe vídeos em ordenação padrão (mais recentes)
**And** todos os vídeos disponíveis são listados (com paginação)

---

## Cenário: Usuário sem assinatura ativa não pode buscar

**Given** que o usuário está autenticado
**But** sua assinatura expirou
**When** tenta buscar vídeos
**Then** recebe erro 403 Forbidden
**And** é redirecionado para renovar assinatura

---

## Cenário: Busca com autocompletar

**Given** que o usuário começa a digitar no campo de busca
**When** digita "prog"
**Then** o sistema sugere autocompletar com termos populares: "programação", "programador", "programa"
**And** o usuário pode clicar para aplicar uma sugestão
