# MONGOLOID v0.8
-> Mongoloid é um conversor de Oracle para MongoDB com recursos para geração
de scripts com sintaxe básica de coleções, documentos e índices

-> O conversor possui, na versão atual, os seguintes recursos:
    -> Geração de scripts a partir de tabelas e tuplas com chaves primárias
    -> Mapeamento de chaves estrangeiras utilizando métodos de:
        -> Referencing
        -> Embedding
    -> Geração de índices para atributos do tipo unique

# HOW TO USE
-> A build da última versão (.jar) se encontra em /dist/mongoloid.jar

-> A execução pode ser feita com um terminal ou prompt de comando, executando a seguinte linha:
            java -jar mongoloid.jar

-> O programa irá então requisitar as entradas de usuário com explicações na tela

# NOTAS E OBSERVAÇÕES
-> Ao entrar com um arquivo que receberá o script do mongo, se esse mesmo existir ele será sobreescrito

-> O mapeamento de chaves estrangeiras pode ocorrer de diversas formas:
    -> Referencing: irá aplicar embedding com apenas o campo _id_ e seus atributos.
        -> Se uma tabela é referenciada mais de uma vez, o embedding ocorre apenas uma vez
    -> Embedding : irá aplicar embedding com a tupla referenciada pelas chaves estrangeiras e todos os seus atributos
    -> NxN : substitui o mapeamento da tabela atual por updates que realizam embedding dos atributos da tabela para a primeira tabela referenciada

-> O programa pede ao usuário o método preferido para o mapeamento de tuplas para cada tabela
    -> Para cada escolha, o método será aplicado em todas as ocorrências possíveis
