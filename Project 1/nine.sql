select p.cid, cast(p.when as date) as day,((select min(s.cost) from yrb_shipping s where s.weight > sum(b.weight * p.qnty)) + sum(o.price * p.qnty)) cost 
from yrb_purchase p, yrb_book b, yrb_offer o
where b.title = p.title AND b.year = p.year AND p.title = o.title AND p.year = o.year AND p.club = o.club
group by p.cid, p.when
order by p.cid
;
