select terms.id as id, terms.term as text, ngd_matrix.value as dist from ngd_matrix, terms where terms.id = ngd_matrix.term2 and term1 = 501 order by dist asc;

select * from terms where id = 502